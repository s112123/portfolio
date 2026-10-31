package org.demo.backend.post.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.global.api.dto.ApiResponse;
import org.demo.backend.post.application.PostService;
import org.demo.backend.post.application.dto.PostDetail;
import org.demo.backend.post.controller.dto.PostCreateRequest;
import org.demo.backend.post.controller.dto.PostUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 글 등록
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody PostCreateRequest request
    ) {
        Long savedPostId = postService.create(memberId, request);
        URI location = URI.create(String.format("/api/v1/posts/%d", savedPostId));
        return ResponseEntity.created(location).body(ApiResponse.success(savedPostId));
    }

    // 글 조회
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetail>> readById(
            @PathVariable("postId") Long postId
    ) {
        PostDetail postDetail = postService.readById(postId);
        return ResponseEntity.ok().body(ApiResponse.success(postDetail));
    }

    // 글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> updateById(
            @AuthenticationPrincipal Long memberId,
            @PathVariable("postId") Long postId,
            @RequestBody PostUpdateRequest request
    ) {
        postService.update(memberId, postId, request);
        return ResponseEntity.ok().body(ApiResponse.success());
    }

    // 글 삭제

    // 글 목록 → 정렬

    // 글 검색
}
