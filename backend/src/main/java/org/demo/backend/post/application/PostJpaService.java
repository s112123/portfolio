package org.demo.backend.post.application;

import lombok.RequiredArgsConstructor;
import org.demo.backend.global.exception.BusinessException;
import org.demo.backend.member.domain.Member;
import org.demo.backend.member.domain.MemberErrorCode;
import org.demo.backend.member.persistence.MemberRepositoryAdapter;
import org.demo.backend.post.application.dto.PostDetail;
import org.demo.backend.post.controller.dto.PostCreateRequest;
import org.demo.backend.post.controller.dto.PostUpdateRequest;
import org.demo.backend.post.domain.Post;
import org.demo.backend.post.domain.PostErrorCode;
import org.demo.backend.post.domain.PostImageProperties;
import org.demo.backend.post.persistence.PostImageRepositoryAdapter;
import org.demo.backend.post.persistence.PostRepositoryAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostJpaService implements PostService {

    private final MemberRepositoryAdapter memberRepositoryAdapter;
    private final PostImageProperties postImageProperties;
    private final PostRepositoryAdapter postRepositoryAdapter;
    private final PostImageRepositoryAdapter postImageRepositoryAdapter;

    @Override
    @Transactional
    public Long create(Long writerId, PostCreateRequest request) {
        // 1. 작성자 조회
        Member writer = memberRepositoryAdapter.findById(writerId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND));

        // 2. 글 등록
        Post post = Post.of(request.getTitle(), request.getContent());
        post.assignMember(writer);
        post.updateThumbUrl(postImageProperties.getDefaultImageUrl());
        Post savedPost = postRepositoryAdapter.save(post);

        // 3. 이미지 파일 정보 변경 (벌크)
        List<String> filenamesInContent = extractFilenames(request.getContent());
        if (!filenamesInContent.isEmpty()) {
            // 이미지 파일 정보 변경
            // 글 등록할 때, 사용된 임시 이미지는 DB 에서 "TEMP" 로 저장되어 있고 FK 는 null 이다
            // 최종 포함된 이미지 파일명은 고유하게 저장되어 있으므로 파일명으로 모두 조회하여 상태를 "ATTACHED" 로 변경한다
            // 그리고 FK 값을 넣어준다
            postImageRepositoryAdapter.attachImages(savedPost, filenamesInContent);
            // 썸네일 경로 업데이트
            savedPost.updateThumbUrl(postImageProperties.getFirstImageUrl(filenamesInContent));
        }

        // TODO: 팔로우 기능이 있다면 팔로워에게 비동기 알림 전송 → 트랜잭션 롤백 주의
        return savedPost.getId();
    }

    @Override
    public PostDetail readById(Long postId) {
        // TODO: 조회수
        return postRepositoryAdapter.fetchById(postId)
                .map(PostDetail::of)
                .orElseThrow(() -> new BusinessException(PostErrorCode.NOT_FOUND));
    }

    @Override
    @Transactional
    public void update(Long writerId, Long postId, PostUpdateRequest request) {
        // 1. 게시물 조회
        Post post = postRepositoryAdapter.fetchById(postId)
                .orElseThrow(() -> new BusinessException(PostErrorCode.NOT_FOUND));
        if (!post.getMember().getId().equals(writerId)) {
            throw new BusinessException(MemberErrorCode.UNAUTHORIZED);
        }

        // 3. 이미지 파일명 조회
        // 기존 글의 첨부된 이미지 파일명 조회
        List<String> oldImageFilenames = extractFilenames(post.getContent());
        // 수정 글의 첨부된 이미지 파일명 조회
        List<String> newImageFilenames = extractFilenames(request.getContent());

        // 4. 글 수정
        if (!newImageFilenames.isEmpty()) {
            post.updateThumbUrl(postImageProperties.getFirstImageUrl(newImageFilenames));
        } else {
            post.updateThumbUrl(postImageProperties.getDefaultImageUrl());
        }
        post.update(request.getTitle(), request.getContent());
        Post updatedPost = postRepositoryAdapter.save(post);

        // 5. 각 이미지의 상태 변경
        // 새롭게 추가된 이미지를 추출하고 상태는 TEMP 에서 ATTACHED 로 변경
        List<String> addedFilenames = getAddedFilenames(oldImageFilenames, newImageFilenames);
        if (!addedFilenames.isEmpty()) {
            postImageRepositoryAdapter.attachImages(updatedPost, addedFilenames);
        }
        // 삭제될 기존 이미지를 추출하고 상태는 ATTACHED 에서 TEMP 로 변경
        // TEMP 로 변경해두면 스케쥴러가 DB 와 물리 파일을 처리한다
        // 참고: DB 에서 상태 변경 대신 removedFilenames 을 즉시 삭제를 해도 된다 → 단, 물리 파일 삭제는 직접 처리 필요
        List<String> removedFilenames = getRemovedFilenames(oldImageFilenames, newImageFilenames);
        if (!removedFilenames.isEmpty()) {
            postImageRepositoryAdapter.detachImages(removedFilenames);
        }

        // TODO: 글을 캐시하는 경우, 기존의 캐시를 삭제한다
    }

    // 글 내용에서 <img> 태그에 있는 url 에서 파일 이름을 추출한다
    private List<String> extractFilenames(String content) {
        List<String> filenames = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return filenames;
        }
        // /post-images/ 뒤에 오는 파일 이름을 추출하기 위한 정규표현식
        // 괄호, 공백, 따옴표 전까지의 문자열을 파일명으로 인식
        String regex = "/post-images/([^/\\s\\)\"]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String filename = matcher.group(1);
            // 중복을 제거하고 싶다면 !filenames.contains(filename) 조건 추가 가능
            if (!filenames.contains(filename)) {
                filenames.add(filename);
            }
        }
        return filenames;
    }

    // 새롭게 추가된 이미지 추출
    private List<String> getAddedFilenames(List<String> oldImageFilenames, List<String> newImageFilenames) {
        return newImageFilenames.stream()
                .filter(filename -> !oldImageFilenames.contains(filename))
                .collect(Collectors.toList());
    }

    // 기존에서 삭제된 이미지
    private List<String> getRemovedFilenames(List<String> oldImageFilenames, List<String> newImageFilenames) {
        return oldImageFilenames.stream()
                .filter(filename -> !newImageFilenames.contains(filename))
                .collect(Collectors.toList());
    }
}
