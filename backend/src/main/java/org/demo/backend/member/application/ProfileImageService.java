package org.demo.backend.member.application;

import org.springframework.core.io.Resource;

public interface ProfileImageService {

    // 프로필 이미지 리소스 반환
    Resource getProfileImageResource(String imageFilename);
}
