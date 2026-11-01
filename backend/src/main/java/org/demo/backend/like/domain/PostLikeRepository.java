package org.demo.backend.like.domain;

import java.util.Optional;

public interface PostLikeRepository {

    // 좋아요 등록
    PostLike savePostLike(PostLike postLike);

    // 좋아요 조회
    Optional<PostLike> findByMemberIdAndPostId(Long memberId, Long postId);

    // 좋아요 삭제 (취소)
    void deleteById(Long postLikeId);

    // 좋아요 여부
    boolean existsByMemberIdAndPostId(Long memberId, Long postId);
}
