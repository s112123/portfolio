package org.demo.backend.post.event;

import lombok.Getter;

@Getter
public class PostHitsChangeEvent {

    public static PostHitsChangeEvent of() {
        return new PostHitsChangeEvent();
    }
}
