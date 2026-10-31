package org.demo.backend.post.application.dto;

import lombok.Getter;
import lombok.ToString;
import org.demo.backend.post.domain.PostImage;

@Getter
@ToString
public class PostImageDetail {

    private Long id;
    private String originalFilename;
    private String storedFilename;
    private String url;

    public static PostImageDetail of(PostImage postImage) {
        PostImageDetail detail = new PostImageDetail();
        detail.id = postImage.getId();
        detail.originalFilename = postImage.getOriginalFilename();
        detail.storedFilename = postImage.getStoredFilename();
        detail.url = postImage.getUrl();
        return detail;
    }
}
