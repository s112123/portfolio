package org.demo.backend.member.application.dto;

import lombok.Getter;
import org.demo.backend.member.domain.Member;
import org.demo.backend.member.domain.MemberRole;

import java.time.LocalDateTime;

@Getter
public class MemberDetail {

    private Long id;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private MemberRole role;
    private ProfileImageDetail profileImage;
    private LocalDateTime registeredAt;

    public static MemberDetail of(Member member) {
        MemberDetail detail = new MemberDetail();
        detail.id = member.getId();
        detail.email = member.getEmail();
        detail.nickname = member.getNickname();
        detail.profileImageUrl = member.getProfileImageUrl();
        detail.role = member.getRole();
        if (member.getProfileImage() != null) {
            detail.profileImage = ProfileImageDetail.of(member.getProfileImage());
        }
        detail.registeredAt = member.getRegisteredAt();
        return detail;
    }
}
