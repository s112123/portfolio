package org.demo.backend.member.application.dto;

import lombok.Getter;
import org.demo.backend.member.domain.ProfileImage;

@Getter
public class ProfileImageDetail {

    private String originalFileName;
    private String savedFileName;
    private String url;

    public static ProfileImageDetail of(ProfileImage profileImage) {
        ProfileImageDetail detail = new ProfileImageDetail();
        detail.originalFileName = profileImage.getOriginalFileName();
        detail.savedFileName = profileImage.getSavedFileName();
        detail.url = profileImage.getUrl();
        return detail;
    }
}
