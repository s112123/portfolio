package org.demo.backend.post.domain;

import org.demo.backend.global.api.dto.PageResult;

import java.util.Optional;

public interface PostRepository {

    // 글 등록
    Post save(Post post);

    // 글 조회
    Optional<Post> findById(Long postId);

    // 글 조회
    Optional<Post> fetchById(Long postId);

    // 좋아요 수 +1 증가
    int incrementPostLike(Long postId);

    // 좋아요 수 -1 감소
    int decrementPostLike(Long postId);

    // 글 목록
    PageResult<Post> fetchAllInfiniteScroll(
            SortType sortType,
            Long lastPostId,
            Long lastValue,
            int size,
            String keyword
    );

    // 특정 회원의 글 목록
    PageResult<Post> fetchPostsByDeferredJoinNative(Long memberId, int page, int size);

    // 글 삭제
}
