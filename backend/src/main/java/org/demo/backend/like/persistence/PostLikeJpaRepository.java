package org.demo.backend.like.persistence;

import org.demo.backend.like.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeJpaRepository extends JpaRepository<PostLike, Long> {

    // 좋아요 조회
    Optional<PostLike> findByMemberIdAndPostId(Long memberId, Long postId);

    // 좋아요 여부
    boolean existsByMemberIdAndPostId(Long memberId, Long postId);
}
