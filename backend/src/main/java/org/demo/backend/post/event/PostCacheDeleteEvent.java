package org.demo.backend.post.event;

import lombok.Getter;

import java.util.List;

@Getter
public class PostCacheDeleteEvent {

    private final List<Long> postIds;

    private PostCacheDeleteEvent(List<Long> postIds) {
        this.postIds = postIds;
    }

    public static PostCacheDeleteEvent of(List<Long> postIds) {
        return new PostCacheDeleteEvent(postIds);
    }
}
