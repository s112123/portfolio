package org.demo.backend.like.application;

public interface PostLikeService {

    // 좋아요 등록
    void like(Long memberId, Long postId);

    // 좋아요 취소
    void cancelLike(Long memberId, Long postId);

    // 좋아요 여부
    boolean existsByMemberIdAndPostId(Long memberId, Long postId);
}
