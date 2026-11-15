package org.demo.backend.comment.persistence;

import org.demo.backend.comment.domain.Comment;
import org.demo.backend.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentJpaRepository extends JpaRepository<Comment, Long> {

    // 댓글 목록 → DB 낮은 버전
    // Java Tree 로 처리하기 위해 DB 페이지네이션을 사용하지 않고 전체 댓글을 조회한다
    // 일반적인 댓글 개수 수준에선 쓸만함 (단, 수만 댓글에는 성능 저하)
    @Query(
            value = "SELECT " +
                    "   c.id AS id, " +
                    "   c.content AS content, " +
                    "   c.registered_at AS registered_at, " +
                    "   c.parent_id AS parent_id, " +
                    "   m.id AS writer_id, " +
                    "   m.nickname AS writer_nickname, " +
                    "   m.profile_image_url AS writer_profile_image_url, " +
                    "   p.member_id AS post_writer_id, " +
                    "   pm.nickname AS parent_writer_nickname, " +
                    "   0 AS reply_count " +
                    "FROM comment c " +
                    "JOIN member m " +
                    "   ON c.member_id = m.id " +
                    "JOIN post p " +
                    "   ON c.post_id = p.id " +
                    "LEFT JOIN comment pc " +
                    "   ON c.parent_id = pc.id " +
                    "LEFT JOIN member pm " +
                    "   ON pc.member_id = pm.id " +
                    "WHERE c.post_id = :postId " +
                    "ORDER BY c.id ASC",
            nativeQuery = true
    )
    List<CommentProjection> fetchPageByPostId(
            @Param("postId") Long postId
    );

    // 댓글 목록 (Recursive)
    // 재귀는 MariaDB 10.2.2+ 또는 MySQL 8.0 이상부터 지원한다
    // 현재 호스팅 (cafe24) 는 MariaDB 10.1.13 이므로 아래 쿼리 불가
    @Query(
            value = "WITH RECURSIVE CommentCTE AS (" +
                    "    SELECT id, content, registered_at, post_id, member_id, parent_id, " +
                    "           CAST(LPAD(id, 10, '0') AS CHAR(200)) AS path, " +
                    "           0 AS depth " +
                    "    FROM comment " +
                    "    WHERE post_id = :postId AND parent_id IS NULL " +
                    "    UNION ALL " +
                    "    SELECT c.id, c.content, c.registered_at, c.post_id, c.member_id, c.parent_id, " +
                    "           CONCAT(cte.path, ' > ', LPAD(c.id, 10, '0')), " +
                    "           cte.depth + 1 " +
                    "    FROM comment c " +
                    "    JOIN CommentCTE cte ON c.parent_id = cte.id " +
                    "    WHERE c.post_id = :postId" +
                    "), " +
                    "CommentReplyCount AS (" +
                    "    SELECT parent.id, COUNT(child.id) AS reply_count " +
                    "    FROM CommentCTE parent " +
                    "    LEFT JOIN CommentCTE child " +
                    "           ON child.path LIKE CONCAT(RTRIM(parent.path), ' > %') " +
                    "    GROUP BY parent.id " +
                    ") " +
                    "SELECT c.id AS id, " +
                    "       c.content AS content, " +
                    "       c.registered_at AS registered_at, " +
                    "       c.parent_id AS parent_id, " +
                    "       m.id AS writer_id, " +
                    "       m.nickname AS writer_nickname, " +
                    "       m.profile_image_url AS writer_profile_image_url, " +
                    "       p.member_id AS post_writer_id, " +
                    "       pm.nickname AS parent_writer_nickname, " +
                    "       COALESCE(rc.reply_count, 0) AS reply_count " +
                    "FROM CommentCTE cte " +
                    "JOIN comment c ON cte.id = c.id " +
                    "JOIN member m ON c.member_id = m.id " +
                    "JOIN post p ON c.post_id = p.id " +
                    "LEFT JOIN comment parent_comment ON c.parent_id = parent_comment.id " +
                    "LEFT JOIN member pm ON parent_comment.member_id = pm.id " +
                    "LEFT JOIN CommentReplyCount rc ON rc.id = cte.id " +
                    "ORDER BY cte.path ASC",
            countQuery = "SELECT COUNT(*) FROM comment WHERE post_id = :postId",
            nativeQuery = true
    )
    Page<CommentProjection> fetchPageByPostId(
            @Param("postId") Long postId,
            Pageable pageable
    );

    // 특정 게시물의 모든 댓글 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "DELETE FROM Comment c " +
                    "WHERE c.post.id IN (:postIds)"
    )
    int deleteByPostIdIn(
            @Param("postIds") List<Long> postIds
    );

    // 특정 회원의 모든 댓글 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "DELETE FROM Comment c " +
                    "WHERE c.post.id IN ( " +
                    "   SELECT p.id " +
                    "   FROM Post p " +
                    "   WHERE p.member.id = :memberId " +
                    ")"
    )
    int deleteByMemberId(
            @Param("memberId") Long memberId
    );

    // 글 삭제 (배치)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "DELETE FROM Comment c " +
                    "WHERE c.id IN (:ids)"
    )
    int deleteByIdIn(@Param("ids") List<Long> ids);

    // 특정 회원의 댓글 목록 (Cursor)
    @Query(
            value = "SELECT c " +
                    "FROM Comment c " +
                    "WHERE c.writer.id = :memberId " +
                    "  AND (:lastCommentId IS NULL OR c.id < :lastCommentId) " +
                    "ORDER BY c.id DESC"
    )
    Slice<Comment> findCursorByMemberIdAndIdLessThan(
            @Param("memberId") Long memberId,
            @Param("lastCommentId") Long lastCommentId,
            Pageable pageable
    );
}
