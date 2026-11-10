package org.demo.backend.post.application;

import lombok.RequiredArgsConstructor;
import org.demo.backend.comment.application.CommentService;
import org.demo.backend.global.exception.BusinessException;
import org.demo.backend.global.web.dto.CursorDetail;
import org.demo.backend.global.web.dto.PageDetail;
import org.demo.backend.like.application.PostLikeService;
import org.demo.backend.member.domain.Member;
import org.demo.backend.member.domain.MemberErrorCode;
import org.demo.backend.member.persistence.MemberRepositoryAdapter;
import org.demo.backend.post.application.dto.PostDetail;
import org.demo.backend.post.controller.dto.PostCreateRequest;
import org.demo.backend.post.controller.dto.PostUpdateRequest;
import org.demo.backend.post.domain.*;
import org.demo.backend.post.domain.cache.PostHitsCacheRepository;
import org.demo.backend.post.domain.cache.PostListCacheRepository;
import org.demo.backend.post.event.PostCacheDeleteEvent;
import org.demo.backend.post.event.PostCreateEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostJpaService implements PostService {

    private final MemberRepositoryAdapter memberRepository;
    private final PostImageProperties postImageProperties;
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final PostHitsCacheRepository postHitsCacheRepository;
    private final PostListCacheRepository postListCacheRepository;
    private final CommentService commentService;
    private final PostLikeService postLikeService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Long save(Long writerId, PostCreateRequest request) {
        // 1. 작성자 조회
        Member writer = memberRepository.findById(writerId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND));

        // 2. 글 등록
        Post post = Post.of(request.getTitle(), request.getContent());
        post.assignMember(writer);
        post.updateThumbUrl(postImageProperties.getDefaultImageUrl());
        Post savedPost = postRepository.save(post);

        // 3. 이미지 파일 정보 변경 (벌크)
        List<String> filenamesInContent = extractFilenames(request.getContent());
        if (!filenamesInContent.isEmpty()) {
            // 이미지 파일 정보 변경
            // 글 등록할 때, 사용된 임시 이미지는 DB 에서 "TEMP" 로 저장되어 있고 FK 는 null 이다
            // 최종 포함된 이미지 파일명은 고유하게 저장되어 있으므로 파일명으로 모두 조회하여 상태를 "ATTACHED" 로 변경한다
            // 그리고 FK 값을 넣어준다
            postImageRepository.attachImageByFilenameIn(savedPost, filenamesInContent);
            // 썸네일 경로 업데이트
            savedPost.updateThumbUrl(postImageProperties.getFirstImageUrl(filenamesInContent));
        }

        // 4. 캐시 등록
        eventPublisher.publishEvent(PostCreateEvent.of(savedPost));
        return savedPost.getId();
    }

    @Override
    public PostDetail fetchByIdAndIncrementHitsCache(Long postId, String viewer, boolean liked) {
        // 1. 게시물 조회
        Post post = postRepository.fetchById(postId)
                .orElseThrow(() -> new BusinessException(PostErrorCode.NOT_FOUND));
        // 2. 조회 수 증분
        Long incrementedHits = postHitsCacheRepository.addViewerAndIncrementDelta(postId, viewer);
        return PostDetail.detailOf(post, post.getHits() + incrementedHits, liked);
    }

    @Override
    @Transactional
    public void update(Long writerId, Long postId, PostUpdateRequest request) {
        // 1. 게시물 조회
        Post post = postRepository.fetchById(postId)
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
        Post updatedPost = postRepository.save(post);

        // 5. 각 이미지의 상태 변경
        // 새롭게 추가된 이미지를 추출하고 상태는 TEMP 에서 ATTACHED 로 변경
        List<String> addedFilenames = getAddedFilenames(oldImageFilenames, newImageFilenames);
        if (!addedFilenames.isEmpty()) {
            postImageRepository.attachImageByFilenameIn(updatedPost, addedFilenames);
        }
        // 삭제될 기존 이미지를 추출하고 상태는 ATTACHED 에서 TEMP 로 변경
        // TEMP 로 변경해두면 스케쥴러가 DB 와 물리 파일을 처리한다
        // 참고: DB 에서 상태 변경 대신 removedFilenames 을 즉시 삭제를 해도 된다 → 단, 물리 파일 삭제는 직접 처리 필요
        List<String> removedFilenames = getRemovedFilenames(oldImageFilenames, newImageFilenames);
        if (!removedFilenames.isEmpty()) {
            postImageRepository.detachImageByFilenameIn(removedFilenames);
        }
    }

    @Override
    public CursorDetail<PostDetail> fetchCursorById(
            SortType sortType,
            Long lastPostId,
            Long lastValue,
            int size,
            String keyword
    ) {
        // 캐시 조회 조건
        // 검색어가 있는 경우, 글 목록으로 캐시하지 않는다
        // 최신 순만 3페이지 목록 캐시한다
        boolean isCacheable = (keyword == null || keyword.trim().isEmpty())
                && sortType == SortType.LATEST;

        // 1. 캐시 조회
        if (isCacheable) {
            // hasNext 값을 구하기 위해 size + 1 만큼 조회한다
            List<Long> cachedPostIds = postListCacheRepository.findPostList(sortType, lastPostId, size + 1);

            // 2. 캐시 히트
            if (cachedPostIds != null && !cachedPostIds.isEmpty()) {
                Long firstId = cachedPostIds.get(0);
                // 캐시 경계에 도달하지 않았으면 캐시에서 조회하고 캐시 경계면 DB 에서 조회한다
                // 현재 로직은 hasNext 확인을 위해 캐시에는 캐시 데이터 + 1 만큼 저장한다
                // 마지막 캐시 페이지에서도 hasNext=true 되어 다음 캐시에서 1개의 데이터가 나온다
                // 그리고 hasNext=false 가 되기 때문에 DB 에 있는 다음 페이지를 DB에서 조회하지 않으므로 바운더리 확인을 해준다
                if (!postListCacheRepository.isPostListCacheBoundary(sortType, firstId)) {
                    boolean hasNext = cachedPostIds.size() > size;
                    cachedPostIds = hasNext ? cachedPostIds.subList(0, size) : cachedPostIds;

                    // IN 절 조회
                    List<Post> posts = postRepository.fetchByIdIn(cachedPostIds);
                    if (posts != null && !posts.isEmpty()) {
                        // 캐시된 ID 순서로 정렬
                        Map<Long, Post> postMap = posts.stream()
                                .collect(Collectors.toMap(p -> p.getId(), Function.identity()));

                        List<PostDetail> postDetails = cachedPostIds.stream()
                                .map(id -> postMap.get(id))
                                .filter(post -> Objects.nonNull(post))
                                .map(post -> PostDetail.summaryOf(post))
                                .collect(Collectors.toList());

                        return CursorDetail.<PostDetail>builder()
                                .content(postDetails)
                                .hasNext(hasNext)
                                .build();
                    }
                }
            }
        }

        // 3. 캐시 미스 → DB 조회
        // 캐시 조회를 위해 11개 조회하여 캐시에 저장한다
        CursorDetail<Post> cursorDetail = postRepository.fetchCursorByIdLessThan(
                sortType,
                lastPostId,
                lastValue,
                size + 1,
                keyword
        );

        // 4. 캐시 저장
        if (cursorDetail != null && !cursorDetail.getContent().isEmpty() && isCacheable) {
            postListCacheRepository.savePostList(sortType, cursorDetail.getContent());
        }

        // 응답할 때는 10개만 응답한다
        boolean hasNext = cursorDetail.getContent().size() > size;
        List<PostDetail> postDetails = cursorDetail.getContent().stream()
                .limit(10)
                .map(PostDetail::summaryOf)
                .collect(Collectors.toList());
        return CursorDetail.<PostDetail>builder()
                .content(postDetails)
                .hasNext(hasNext)
                .build();
    }

    @Override
    public PageDetail<PostDetail> fetchPageByMemberId(Long memberId, int page, int size) {
        PageDetail<Post> posts = postRepository.fetchPageByMemberId(memberId, page, size);
        return posts.map(post -> PostDetail.summaryOfMyPost(post));
    }

    @Override
    @Transactional
    public void deleteById(Long postId, Long memberId) {
        // 1. 유효성 검사
        Post post = postRepository.fetchById(postId)
                .orElseThrow(() -> new BusinessException(PostErrorCode.NOT_FOUND));
        if (!post.getMember().getId().equals(memberId)) {
            throw new BusinessException(MemberErrorCode.FORBIDDEN);
        }
        // 2. 글 삭제
        deleteByIdIn(Arrays.asList(postId));
    }

    @Override
    @Transactional
    public void deleteByIdIn(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) return;
        // 1. 댓글을 삭제한다 → 댓글이 많으면 청크로 끊어야 한다
        commentService.deleteByPostIdIn(postIds);
        // 2. 좋아요를 삭제한다 → 좋아요가 많으면 청크로 끊어야 한다
        postLikeService.deleteByPostIdIn(postIds);
        // 3. 글에 첨부된 파일의 상태를 TEMP 변경 → 파일 삭제 스케쥴러가 물리 파일을 삭제한다
        postImageRepository.detachImageByIdIn(postIds);
        // 4. 글 정보 삭제
        postRepository.deleteByIdIn(postIds);
        // 5. 글 캐시 삭제 (Redis)
        eventPublisher.publishEvent(PostCacheDeleteEvent.of(postIds));
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
