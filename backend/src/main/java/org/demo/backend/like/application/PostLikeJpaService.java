package org.demo.backend.like.application;

import lombok.RequiredArgsConstructor;
import org.demo.backend.global.exception.BusinessException;
import org.demo.backend.like.domain.PostLike;
import org.demo.backend.like.domain.PostLikeErrorCode;
import org.demo.backend.like.domain.PostLikeRepository;
import org.demo.backend.member.domain.Member;
import org.demo.backend.member.domain.MemberErrorCode;
import org.demo.backend.member.domain.MemberRepository;
import org.demo.backend.post.domain.Post;
import org.demo.backend.post.domain.PostErrorCode;
import org.demo.backend.post.domain.PostRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostLikeJpaService implements PostLikeService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    @Override
    @Transactional
    public void like(Long memberId, Long postId) {
        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND));
        // 2. 포스트 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(PostErrorCode.NOT_FOUND));

        try {
            // 3. 좋아요 등록
            postLikeRepository.save(PostLike.of(member, post));
            // 4. 좋아요 +1 증가 (동시성 문제 해결 → 원자적 업데이트)
            postRepository.incrementLikeById(postId);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(PostLikeErrorCode.ALREADY_LIKES);
        }
    }

    @Override
    @Transactional
    public void cancelLike(Long memberId, Long postId) {
        // 1. 좋아요 조회
        PostLike postLike = postLikeRepository.findByMemberIdAndPostId(memberId, postId)
                .orElseThrow(() -> new BusinessException(PostLikeErrorCode.NOT_FOUND));
        // 2. 좋아요 취소 (삭제)
        postLikeRepository.deleteById(postLike.getId());
        // 3. 좋아요 -1 감소 (동시성 문제 해결 → 원자적 업데이트 + p.likes > 0 조건으로 0 이하 방지)
        postRepository.decrementLikeById(postId);
    }

    @Override
    public boolean existsByMemberIdAndPostId(Long memberId, Long postId) {
        return postLikeRepository.existsByMemberIdAndPostId(memberId, postId);
    }

    @Override
    @Transactional
    public void deleteByPostIdIn(List<Long> postId) {
        postLikeRepository.deleteByPostIdIn(postId);
    }

    @Override
    @Transactional
    public void deleteByMemberId(Long memberId) {
        postLikeRepository.deleteByMemberId(memberId);
    }

    @Override
    public Page<PostLike> findPageByMemberId(Long memberId, int page, int size) {
        return postLikeRepository.fetchPageByMemberId(memberId, page, size);
    }
}
