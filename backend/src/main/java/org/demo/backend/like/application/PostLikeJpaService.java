package org.demo.backend.like.application;

import lombok.RequiredArgsConstructor;
import org.demo.backend.global.exception.BusinessException;
import org.demo.backend.like.domain.PostLike;
import org.demo.backend.like.domain.PostLikeErrorCode;
import org.demo.backend.like.domain.PostLikeRepository;
import org.demo.backend.member.domain.Member;
import org.demo.backend.member.domain.MemberErrorCode;
import org.demo.backend.member.persistence.MemberRepositoryAdapter;
import org.demo.backend.post.domain.Post;
import org.demo.backend.post.domain.PostErrorCode;
import org.demo.backend.post.persistence.PostRepositoryAdapter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeJpaService implements PostLikeService {

    private final MemberRepositoryAdapter memberRepositoryAdapter;
    private final PostRepositoryAdapter postRepositoryAdapter;
    private final PostLikeRepository postLikeRepository;

    @Override
    @Transactional
    public void like(Long memberId, Long postId) {
        // 1. 회원 조회
        Member member = memberRepositoryAdapter.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND));
        // 2. 포스트 조회
        Post post = postRepositoryAdapter.findById(postId)
                .orElseThrow(() -> new BusinessException(PostErrorCode.NOT_FOUND));

        try {
            // 3. 좋아요 등록
            postLikeRepository.savePostLike(PostLike.of(member, post));
            // 4. 좋아요 +1 증가 (동시성 문제 해결 → 원자적 업데이트)
            postRepositoryAdapter.incrementPostLike(postId);
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
        // 2. 좋아요 -1 감소 (동시성 문제 해결 → 원자적 업데이트 + p.likes > 0 조건으로 0 이하 방지)
        postRepositoryAdapter.decrementPostLike(postId);
    }

    @Override
    public boolean existsByMemberIdAndPostId(Long memberId, Long postId) {
        return postLikeRepository.existsByMemberIdAndPostId(memberId, postId);
    }
}
