package org.demo.backend.post.event;

import lombok.Getter;

import java.util.List;

@Getter
public class PostDeleteCacheEvent {

    private final List<Long> postIds;

    private PostDeleteCacheEvent(List<Long> postIds) {
        this.postIds = postIds;
    }

    public static PostDeleteCacheEvent of(List<Long> postIds) {
        return new PostDeleteCacheEvent(postIds);
    }
}
