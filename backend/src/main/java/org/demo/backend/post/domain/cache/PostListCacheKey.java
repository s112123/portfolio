package org.demo.backend.post.domain.cache;

import org.demo.backend.post.domain.SortType;

public abstract class PostListCacheKey {

    // 글 목록 (최신 순)
    private static final String LATEST_KEY = "post:list:latest";

    public static String generate(SortType sortType) {
        if (SortType.LIKES == sortType ) {
        } else if (SortType.HITS == sortType) {
        }
        return LATEST_KEY;
    }
}
