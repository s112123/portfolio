package org.demo.backend.post.event;

import lombok.Getter;

import java.util.List;

@Getter
public class PostDeleteEvent {

    private final List<Long> postIds;

    private PostDeleteEvent(List<Long> postIds) {
        this.postIds = postIds;
    }

    public static PostDeleteEvent of(List<Long> postIds) {
        return new PostDeleteEvent(postIds);
    }
}
