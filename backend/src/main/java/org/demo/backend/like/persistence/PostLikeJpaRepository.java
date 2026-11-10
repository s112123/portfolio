package org.demo.backend.like.persistence;

import org.demo.backend.like.domain.PostLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostLikeJpaRepository extends JpaRepository<PostLike, Long> {

    // 좋아요 조회
    Optional<PostLike> findByMemberIdAndPostId(Long memberId, Long postId);

    // 좋아요 목록
    @Query(
            value = "SELECT pl " +
                    "FROM PostLike pl " +
                    "JOIN FETCH pl.member " +
                    "JOIN FETCH pl.post " +
                    "WHERE pl.member.id = :memberId",
            countQuery = "SELECT COUNT(pl) FROM PostLike pl WHERE pl.member.id = :memberId"
    )
    Page<PostLike> fetchPageByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    // 좋아요 여부
    boolean existsByMemberIdAndPostId(Long memberId, Long postId);

    // 특정 게시물의 좋아요 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "DELETE FROM PostLike pl " +
                    "WHERE pl.post.id IN (:postIds)"
    )
    void deleteByPostIdIn(@Param("postIds") List<Long> postIds);

    // 특정 회원의 좋아요 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "DELETE FROM PostLike pl " +
                    "WHERE pl.member.id = :memberId"
    )
    void deleteByMemberId(@Param("memberId") Long memberId);
}
