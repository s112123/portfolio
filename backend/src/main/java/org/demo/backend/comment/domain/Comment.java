package org.demo.backend.comment.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.demo.backend.global.jpa.BaseTimeEntity;
import org.demo.backend.member.domain.Member;
import org.demo.backend.post.domain.Post;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "comment",
        indexes = {
                @Index(name = "idx_comment_post_id", columnList = "post_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime registeredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member writer;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Comment> children = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "parent_id",
            foreignKey = @ForeignKey(
                    foreignKeyDefinition = "FOREIGN KEY (parent_id) REFERENCES comment (id) ON DELETE CASCADE"
            )
    )
    private Comment parent;

    public static Comment of(
            Member writer,
            String content
    ) {
        Comment comment = new Comment();
        comment.writer = writer;
        comment.content = content;
        comment.registeredAt = LocalDateTime.now();
        return comment;
    }

    public void assignPost(Post post) {
        this.post = post;
    }

    public void assignParent(Comment parent) {
        this.parent = parent;
        parent.getChildren().add(this);
    }
}
