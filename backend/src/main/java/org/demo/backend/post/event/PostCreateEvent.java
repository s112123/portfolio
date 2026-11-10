package org.demo.backend.post.event;

import lombok.Getter;
import org.demo.backend.post.domain.Post;

@Getter
public class PostCreateEvent {

    private final Long postId;

    private PostCreateEvent(Long postId) {
        this.postId = postId;
    }

    public static PostCreateEvent of(Post post) {
        return new PostCreateEvent(post.getId());
    }
}
