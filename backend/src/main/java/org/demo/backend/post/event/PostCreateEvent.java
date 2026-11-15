package org.demo.backend.post.event;

import lombok.Getter;
import org.demo.backend.post.application.dto.PostDetail;
import org.demo.backend.post.domain.Post;

@Getter
public class PostCreateEvent {

    private final PostDetail postDetail;

    private PostCreateEvent(Post post) {
        this.postDetail = PostDetail.summaryOf(post);
    }

    public static PostCreateEvent of(Post post) {
        return new PostCreateEvent(post);
    }
}
