package org.demo.backend.member.application;

import lombok.RequiredArgsConstructor;
import org.demo.backend.global.exception.BusinessException;
import org.demo.backend.member.domain.ProfileImageErrorCode;
import org.demo.backend.member.domain.ProfileImageProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class ProfileImageJpaService implements ProfileImageService {

    private final ProfileImageProperties profileImageProperties;
    private final ResourceLoader resourceLoader;

    @Override
    public Resource getProfileImageResource(String imageFilename) {
        // 이미지 리소스 반환
        try {
            Resource resource = getImageFileResource(imageFilename);
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessException(ProfileImageErrorCode.NOT_FOUND);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("파일 경로가 잘못되었습니다: " + imageFilename, e);
        }
    }

    // 리소스 추출
    private Resource getImageFileResource(String filename) throws MalformedURLException {
        // 1. 기본 이미지인 경우 → classpath 에 있는 파일은 ResourceLoader 로 처리한다
        if (profileImageProperties.getDefaultImage().getFile().equals(filename)) {
            String classpathLocation = profileImageProperties.getDefaultImage().getPath() + filename;
            return resourceLoader.getResource(classpathLocation);
        }
        // 2. 일반 업로드 파일인 경우 (외부 스토리지)
        Path filePath = Paths.get(profileImageProperties.getStorage(), filename);
        return new UrlResource(filePath.toUri());
    }
}
