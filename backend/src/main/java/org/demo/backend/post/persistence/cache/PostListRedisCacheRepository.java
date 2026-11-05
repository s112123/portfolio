package org.demo.backend.post.persistence.cache;

import lombok.RequiredArgsConstructor;
import org.demo.backend.post.application.dto.PostDetail;
import org.demo.backend.post.domain.SortType;
import org.demo.backend.post.domain.cache.PostListCacheKey;
import org.demo.backend.post.domain.cache.PostListCacheRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostListRedisCacheRepository implements PostListCacheRepository {

    private final RedisTemplate<String, String> redisTemplate;;

    @Override
    public void cachePostList(SortType sortType, List<Long> postIds) {
        String key = PostListCacheKey.generate(sortType);
    }
}
