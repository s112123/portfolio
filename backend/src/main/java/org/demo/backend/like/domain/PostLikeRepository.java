package org.demo.backend.like.domain;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository {

    // 좋아요 등록
    PostLike save(PostLike postLike);

    // 좋아요 조회
    Optional<PostLike> findByMemberIdAndPostId(Long memberId, Long postId);

    // 좋아요 목록
    Page<PostLike> fetchPageByMemberId(Long memberId, int page, int size);

    // 좋아요 삭제 (취소)
    void deleteById(Long postLikeId);

    // 좋아요 여부
    boolean existsByMemberIdAndPostId(Long memberId, Long postId);

    // 특정 게시물의 좋아요 삭제
    void deleteByPostIdIn(List<Long> postIds);

    // 특정 회원의 좋아요 삭제
    void deleteByMemberId(Long memberId);
}
