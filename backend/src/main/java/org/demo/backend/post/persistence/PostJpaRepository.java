package org.demo.backend.post.persistence;

import org.demo.backend.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostJpaRepository extends JpaRepository<Post, Long> {

    // 글 조회
    @Query(
            value = "SELECT p " +
                    "FROM Post p " +
                    "LEFT JOIN FETCH p.member m " +
                    "LEFT JOIN FETCH m.profileImage " +
                    "LEFT JOIN FETCH p.postImages pi " +
                    "WHERE p.id = :postId"
    )
    Optional<Post> fetchById(@Param("postId") Long postId);

    // 좋아요 수 +1 증가
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "UPDATE Post p " +
                    "SET p.likes = p.likes + 1 " +
                    "WHERE p.id = :postId"
    )
    int incrementPostLike(@Param("postId") Long postId);

    // 좋아요 수 -1 감소
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "UPDATE Post p " +
                    "SET p.likes = p.likes - 1 " +
                    "WHERE p.id = :postId AND p.likes > 0"
    )
    int decrementPostLike(@Param("postId") Long postId);
}
