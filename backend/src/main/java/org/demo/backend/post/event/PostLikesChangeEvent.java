package org.demo.backend.post.event;

import lombok.Getter;

@Getter
public class PostLikesChangeEvent {

    public static PostLikesChangeEvent of() {
        return new PostLikesChangeEvent();
    }
}
