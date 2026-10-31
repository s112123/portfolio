package org.demo.backend.post.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.global.api.dto.ApiResponse;
import org.demo.backend.global.file.dto.FileUploadRequest;
import org.demo.backend.post.application.PostImageService;
import org.demo.backend.post.application.dto.PostImageDetail;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Slf4j
@RestController
@RequestMapping("/api/v1/post-images")
@RequiredArgsConstructor
public class PostImageController {

    private final PostImageService postImageService;

    // 첨부 이미지 업로드
    @PostMapping
    public ResponseEntity<ApiResponse<PostImageDetail>> uploadPostImageFile(
            @ModelAttribute FileUploadRequest request
    ) {
        PostImageDetail postImageDetail = postImageService.uploadPostImage(request);
        return ResponseEntity.ok().body(ApiResponse.success(postImageDetail));
    }

    // 첨부 이미지 조회
    @GetMapping("/{imageFilename}")
    public ResponseEntity<Resource> readPostImageFile(
            @PathVariable("imageFilename") String imageFilename
    ) {
        InputStream inputStream = postImageService.getPostImageInputStream(imageFilename);
        InputStreamResource resource = new InputStreamResource(inputStream);

        String contentType;
        try {
            contentType = Files.probeContentType(resource.getFile().toPath());
        } catch (IOException e) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
    }
}
