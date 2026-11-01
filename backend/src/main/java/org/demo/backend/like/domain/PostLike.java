package org.demo.backend.like.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.demo.backend.global.jpa.BaseTimeEntity;
import org.demo.backend.member.domain.Member;
import org.demo.backend.post.domain.Post;

import javax.persistence.*;

@Entity
@Table(
        name = "post_like",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_post",
                        columnNames = {"member_id", "post_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    public static PostLike of(Member member, Post post) {
        PostLike postLike = new PostLike();
        postLike.member = member;
        postLike.post = post;
        return postLike;
    }
}
