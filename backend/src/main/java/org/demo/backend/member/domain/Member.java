package org.demo.backend.member.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.demo.backend.global.jpa.BaseTimeEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Column(nullable = false)
    private LocalDateTime registeredAt;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProfileImage profileImage;

    public static Member of(
            String email,
            String encodedPassword,
            String nickname,
            String defaultProfileImage,
            String defaultProfileImageUrl,
            LocalDateTime registeredAt
    ) {
        Member member = new Member();
        member.email = email;
        member.password = encodedPassword;
        member.nickname = nickname;
        member.role = MemberRole.USER;
        member.registeredAt = registeredAt;
        assignDefaultProfileImage(member, defaultProfileImage, defaultProfileImageUrl);
        return member;
    }

    // 기본 프로필 이미지 등록
    private static void assignDefaultProfileImage(
            Member member,
            String defaultProfileImage,
            String defaultProfileImageUrl
    ) {
        ProfileImage profileImage = ProfileImage.of(defaultProfileImage, defaultProfileImage, defaultProfileImageUrl);
        member.profileImage = profileImage;
        profileImage.assignMember(member);
    }
}
