package org.demo.backend.post.persistence;

import org.demo.backend.post.domain.Post;
import org.demo.backend.post.domain.PostImageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostJpaRepository extends JpaRepository<Post, Long> {

    // 글 조회
    @Query(value = "" +
            "SELECT p " +
            "FROM Post p " +
            "LEFT JOIN FETCH p.member m " +
            "LEFT JOIN FETCH m.profileImage " +
            "LEFT JOIN FETCH p.postImages pi " +
            "WHERE p.id = :postId")
    Optional<Post> fetchById(@Param("postId") Long postId);
}
