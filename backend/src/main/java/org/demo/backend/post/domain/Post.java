package org.demo.backend.post.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.demo.backend.global.jpa.BaseTimeEntity;
import org.demo.backend.member.domain.Member;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Long hits;

    @Column(nullable = false)
    private Long likes;

    @Column(nullable = false)
    private String thumbUrl;

    @Column(nullable = false)
    private LocalDateTime registeredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> postImages = new ArrayList<>();

    public static Post of(String title, String content) {
        Post post = new Post();
        post.title = title;
        post.content = content;
        post.hits = 0L;
        post.likes = 0L;
        post.registeredAt = LocalDateTime.now();
        return post;
    }

    public void assignMember(Member member) {
        this.member = member;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void updateThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }
}
