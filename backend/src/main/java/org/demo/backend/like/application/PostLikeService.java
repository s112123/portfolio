package org.demo.backend.like.application;

import org.demo.backend.like.domain.PostLike;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PostLikeService {

    // 좋아요 등록
    void like(Long memberId, Long postId);

    // 좋아요 취소
    void cancelLike(Long memberId, Long postId);

    // 좋아요 여부
    boolean existsByMemberIdAndPostId(Long memberId, Long postId);

    // 특정 게시물의 좋아요 삭제 (배치)
    void deleteByPostIdIn(List<Long> postId);

    // 특정 회원의 좋아요 삭제
    void deleteByMemberId(Long memberId);

    // 좋아요 목록
    Page<PostLike> findPageByMemberId(Long memberId, int page, int size);
}
