package org.demo.backend.post.domain.cache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.demo.backend.post.domain.SortType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostListCacheKey {

    // 글 목록 (최신 순)
    private static final String LATEST_KEY = "post:list:latest";
    // 글 목록 (좋아요 순)
    private static final String LIKES_KEY = "post:list:likes";
    // 글 목록 (조회 순)
    private static final String HITS_KEY = "post:list:hits";

    public static String generatePostListKey(SortType sortType) {
        if (SortType.LIKES == sortType ) return LIKES_KEY;
        else if (SortType.HITS == sortType) return HITS_KEY;
        return LATEST_KEY;
    }
}
