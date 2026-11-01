package org.demo.backend.post.application.dto;

import lombok.Getter;
import lombok.ToString;
import org.demo.backend.member.application.dto.MemberDetail;
import org.demo.backend.post.domain.Post;
import org.demo.backend.post.domain.PostImageStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
public class PostDetail {

    private Long id;
    private MemberDetail writer;
    private String title;
    private String content;
    private Long hits;
    private Long likes;
    private boolean liked;
    private List<PostImageDetail> images;
    private LocalDateTime registeredAt;

    public static PostDetail of(Post post, Long hits, boolean liked) {
        PostDetail detail = new PostDetail();
        detail.id = post.getId();
        detail.writer = MemberDetail.of(post.getMember());
        detail.title = post.getTitle();
        detail.content = post.getContent();
        detail.hits = hits;
        detail.likes = post.getLikes();
        detail.liked = liked;
        detail.images = post.getPostImages().stream()
                .filter(image -> image.getStatus() == PostImageStatus.ATTACHED)
                .map(PostImageDetail::of)
                .collect(Collectors.toList());
        detail.registeredAt = post.getRegisteredAt();
        return detail;
    }
}
