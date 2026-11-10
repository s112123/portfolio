package org.demo.backend.post.domain.cache;

import org.demo.backend.post.domain.Post;
import org.demo.backend.post.domain.SortType;

import java.util.List;

public interface PostListCacheRepository {

    // 글의 ID 캐시
    void savePostId(SortType sortType, Long postId);

    // 글의 ID 목록 캐시
    void savePostList(SortType sortType, List<Post> posts);

    // 글 목록 캐시 조회
    List<Long> findPostList(SortType sortType, Long lastPostId, int size);

    // 캐시 경계인지 여부
    boolean isPostListCacheBoundary(SortType sortType, Long value);

    // 캐시 삭제
    void deletePostList(SortType sortType, List<Long> postIds);
}
