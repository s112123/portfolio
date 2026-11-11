package org.demo.backend.post.event;

import lombok.Getter;
import org.demo.backend.post.domain.Post;

@Getter
public class PostCreateCacheEvent {

    private final Long postId;

    private PostCreateCacheEvent(Long postId) {
        this.postId = postId;
    }

    public static PostCreateCacheEvent of(Post post) {
        return new PostCreateCacheEvent(post.getId());
    }
}
