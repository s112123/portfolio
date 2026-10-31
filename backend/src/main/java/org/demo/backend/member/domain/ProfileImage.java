package org.demo.backend.member.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "profile_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String savedFileName;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String filePath;

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public static ProfileImage of(
            String originalFileName,
            String savedFileName,
            String filePath,
            String url
    ) {
        ProfileImage profileImage = new ProfileImage();
        profileImage.originalFileName = originalFileName;
        profileImage.savedFileName = savedFileName;
        profileImage.filePath = filePath;
        profileImage.url = url;
        return profileImage;
    }

    public void assignMember(Member member) {
        this.member = member;
    }
}
