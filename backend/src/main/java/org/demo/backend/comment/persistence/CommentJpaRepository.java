package org.demo.backend.comment.persistence;

import org.demo.backend.comment.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CommentJpaRepository extends JpaRepository<Comment, Long> {

    // 댓글 목록 (Recursive)
    @Query(
            value = "WITH RECURSIVE CommentCTE AS (" +
                    "    SELECT id, content, registered_at, post_id, member_id, parent_id, " +
                    "           CAST(id AS CHAR(200)) AS path, " +
                    "           0 AS depth " +
                    "    FROM comment " +
                    "    WHERE post_id = :postId AND parent_id IS NULL " +
                    "    UNION ALL " +
                    "    SELECT c.id, c.content, c.registered_at, c.post_id, c.member_id, c.parent_id, " +
                    "           CONCAT(cte.path, ' > ', c.id), " +
                    "           cte.depth + 1 " +
                    "    FROM comment c " +
                    "    JOIN CommentCTE cte ON c.parent_id = cte.id " +
                    "    WHERE c.post_id = :postId" +
                    ") " +
                    "SELECT c.id AS id, " +
                    "       c.content AS content, " +
                    "       c.registered_at AS registered_at, " +
                    "       c.parent_id AS parent_id, " +
                    "       m.id AS writer_id, " +
                    "       m.nickname AS writer_nickname, " +
                    "       m.profile_image_url AS writer_profile_image_url, " +
                    "       p.member_id AS post_writer_id, " +
                    "       pm.nickname AS parent_writer_nickname " +
                    "FROM CommentCTE cte " +
                    "JOIN comment c ON cte.id = c.id " +
                    "JOIN member m ON c.member_id = m.id " +
                    "JOIN post p ON c.post_id = p.id " +
                    "LEFT JOIN comment parent_comment ON c.parent_id = parent_comment.id " +
                    "LEFT JOIN member pm ON parent_comment.member_id = pm.id " +
                    "ORDER BY cte.path ASC",
            countQuery = "SELECT COUNT(*) FROM comment WHERE post_id = :postId",
            nativeQuery = true
    )
    Page<CommentProjection> fetchAllByPostIdWithRecursive(
            @Param("postId") Long postId,
            Pageable pageable
    );
}
