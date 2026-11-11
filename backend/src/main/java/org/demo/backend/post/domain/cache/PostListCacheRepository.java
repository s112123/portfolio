package org.demo.backend.post.domain.cache;

import org.demo.backend.post.application.dto.PostDetail;
import org.demo.backend.post.domain.Post;
import org.demo.backend.post.domain.SortType;

import java.util.List;

public interface PostListCacheRepository {

    // 글의 ID 목록 캐시
    void savePostList(SortType sortType, List<PostDetail> posts);

    // 글 목록 캐시 조회
    List<Long> findPostList(SortType sortType, Long lastPostId, int size);

    // 캐시 삭제
    void deletePostList(SortType sortType, List<Long> postIds);

    // 캐시 무효화
    void deleteKey(SortType sortType);

    // 키 존재 여부
    boolean hasKey(SortType sortType);
}
