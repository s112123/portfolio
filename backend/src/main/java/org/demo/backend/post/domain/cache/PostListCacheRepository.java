package org.demo.backend.post.domain.cache;

import org.demo.backend.post.application.dto.PostDetail;
import org.demo.backend.post.domain.SortType;

import java.util.List;

public interface PostListCacheRepository {

    // 글 목록 캐시
    void cachePostList(SortType sortType, List<Long> postIds);
}
