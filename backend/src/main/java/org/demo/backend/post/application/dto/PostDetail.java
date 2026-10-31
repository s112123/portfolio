package org.demo.backend.post.application.dto;

import lombok.Getter;
import lombok.ToString;
import org.demo.backend.post.domain.Post;
import org.demo.backend.post.domain.PostImage;
import org.demo.backend.post.domain.PostImageStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
public class PostDetail {

    private Long id;
    private String writer;
    private String title;
    private String content;
    private Long hits;
    private Long likes;
    private List<PostImageDetail> images;
    private LocalDateTime registeredAt;

    public static PostDetail of(Post post) {
        PostDetail detail = new PostDetail();
        detail.id = post.getId();
        detail.writer = post.getMember().getNickname();
        detail.title = post.getTitle();
        detail.content = post.getContent();
        detail.hits = post.getHits();
        detail.likes = post.getLikes();
        detail.images = post.getPostImages().stream()
                .filter(image -> image.getStatus() == PostImageStatus.ATTACHED)
                .map(PostImageDetail::of)
                .collect(Collectors.toList());
        detail.registeredAt = post.getRegisteredAt();
        return detail;
    }
}
