package org.demo.backend.post.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.demo.backend.global.jpa.BaseTimeEntity;

import javax.persistence.*;

@Entity
@Table(
        name = "post_image",
        indexes = {
                @Index(name = "idx_post_image_status_created_at", columnList = "status, created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false, unique = true)
    private String storedFilename;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostImageStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    public static PostImage of(
            String originalFilename,
            String storedFilename,
            String filePath,
            String url,
            PostImageStatus status
    ) {
        PostImage postImage = new PostImage();
        postImage.originalFilename = originalFilename;
        postImage.storedFilename = storedFilename;
        postImage.filePath = filePath;
        postImage.url = url;
        postImage.status = status;
        return postImage;
    }
}
