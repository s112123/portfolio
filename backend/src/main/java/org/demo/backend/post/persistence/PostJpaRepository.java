package org.demo.backend.post.persistence;

import org.demo.backend.post.domain.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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
    int incrementLikeById(@Param("postId") Long postId);

    // 좋아요 수 -1 감소
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "UPDATE Post p " +
                    "SET p.likes = p.likes - 1 " +
                    "WHERE p.id = :postId AND p.likes > 0"
    )
    int decrementLikeById(@Param("postId") Long postId);


    // 글 목록 (IN)
    @Query(
            value = "SELECT p " +
                    "FROM Post p " +
                    "JOIN FETCH p.member m " +
                    "LEFT JOIN FETCH m.profileImage " +
                    "WHERE p.id IN (:postIds) " +
                    "ORDER BY p.id DESC"
    )
    List<Post> fetchByIdIn(@Param("postIds") List<Long> postIds);

    // 글 목록 (무한 스크롤) → 최신 순
    @Query(
            value = "SELECT p " +
                    "FROM Post p " +
                    "JOIN FETCH p.member m " +
                    "LEFT JOIN FETCH m.profileImage " +
                    "WHERE (:lastPostId IS NULL OR p.id < :lastPostId) " +
                    "  AND (:keyword IS NULL OR :keyword = '' " +
                    "       OR p.title LIKE CONCAT('%', :keyword, '%')" +
                    "       OR p.content LIKE CONCAT('%', :keyword, '%')) " +
                    "ORDER BY p.id DESC"
    )
    Slice<Post> fetchCursorByIdLessThanLatest(
            @Param("lastPostId") Long lastPostId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 글 목록 (무한 스크롤) → 좋아요 순
    // 조회 시점에 좋아요 순위가 급상승하여 마지막 데이터 행 위로 올라간 데이터의 누락 조회는 감수한다
    // LIKE %keyword% 때문에 복합 인덱스 (likes, id) 를 무시하고 Full Table Scan 이 발생한다
    // 목록 조회 시, 검색어 존재 여부에 따라 메서드를 검색용으로 분리하거나 QueryDsl 로 동적 쿼리를 만든다
    // 또는 모니터링을 하고 있다가 MySQL 의 FullText Index 또는 Elasticsearch 도입을 검토한다
    @Query(
            value = "SELECT p " +
                    "FROM Post p " +
                    "JOIN FETCH p.member m " +
                    "LEFT JOIN FETCH m.profileImage " +
                    "WHERE ((:lastPostId IS NULL OR :lastLikes IS NULL) " +
                    "       OR (p.likes = :lastLikes AND p.id < :lastPostId) " +
                    "       OR p.likes < :lastLikes) " +
                    "  AND (:keyword IS NULL OR :keyword = '' " +
                    "       OR p.title LIKE CONCAT('%', :keyword, '%') " +
                    "       OR p.content LIKE CONCAT('%', :keyword, '%')) " +
                    "ORDER BY p.likes DESC, p.id DESC"
    )
    Slice<Post> fetchCursorByIdLessThanLikes(
            @Param("lastPostId") Long lastPostId,
            @Param("lastLikes") Long lastLikes,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 글 목록 (무한 스크롤) → 조회 순
    @Query(
            value = "SELECT p " +
                    "FROM Post p " +
                    "JOIN FETCH p.member m " +
                    "LEFT JOIN FETCH m.profileImage " +
                    "WHERE ((:lastPostId IS NULL OR :lastHits IS NULL) " +
                    "       OR (p.hits = :lastHits AND p.id < :lastPostId) " +
                    "       OR p.hits < :lastHits) " +
                    "  AND (:keyword IS NULL OR :keyword = '' " +
                    "       OR p.title LIKE CONCAT('%', :keyword, '%') " +
                    "       OR p.content LIKE CONCAT('%', :keyword, '%')) " +
                    "ORDER BY p.hits DESC, p.id DESC"
    )
    Slice<Post> fetchCursorByIdLessThanHits(
            @Param("lastPostId") Long lastPostId,
            @Param("lastHits") Long lastHits,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 특정 회원의 글 목록 (Deferred Join, Covering Index)
    @Query(
            value = "SELECT * " +
                    "FROM post p " +
                    "INNER JOIN (" +
                    "   SELECT id " +
                    "   FROM post " +
                    "   WHERE member_id = :memberId " +
                    "   ORDER BY id DESC " +
                    "   LIMIT :limit OFFSET :offset " +
                    ") t " +
                    "ON p.id = t.id",
            nativeQuery = true
    )
    List<Post> findPageByMemberId(
            @Param("memberId") Long memberId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    // 특정 회원의 글 목록 개수
    @Query(
            value = "SELECT COUNT(*) FROM post WHERE member_id = :memberId",
            nativeQuery = true
    )
    long countByMemberId(@Param("memberId") Long memberId);

    // 특정 회원의 글 목록 (Cursor)
    @Query(
            value = "SELECT p " +
                    "FROM Post p " +
                    "WHERE p.member.id = :memberId " +
                    "  AND (:lastPostId IS NULL OR p.id < :lastPostId) " +
                    "ORDER BY p.id DESC"
    )
    Slice<Post> findCursorByIdLessThenAndMemberId(
            @Param("memberId") Long memberId,
            @Param("lastPostId") Long lastPostId,
            Pageable pageable
    );

    // 글 삭제 (배치)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "DELETE FROM Post p " +
                    "WHERE p.id IN (:ids)"
    )
    int deleteByIdIn(@Param("ids") List<Long> ids);
}
