package org.demo.backend.member.controller;

import lombok.RequiredArgsConstructor;
import org.demo.backend.member.application.ProfileImageService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/v1/profile-images/")
@RequiredArgsConstructor
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    // 프로필 이미지 조회
    @GetMapping("/{imageFilename}")
    public ResponseEntity<?> readProfileImage(
            @PathVariable("imageFilename") String imageFilename
    ) {
        Resource resource = profileImageService.getProfileImageResource(imageFilename);
        String contentType;
        try {
            contentType = Files.probeContentType(resource.getFile().toPath());
        } catch (IOException e) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
    }
}
