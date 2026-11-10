package org.demo.backend.like.controller;

import lombok.RequiredArgsConstructor;
import org.demo.backend.global.web.dto.ApiResponse;
import org.demo.backend.like.application.PostLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/likes")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    // 좋아요 등록
    @PostMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> likePost(
            @AuthenticationPrincipal Long memberId,
            @PathVariable("postId") Long postId
    ) {
        postLikeService.like(memberId, postId);
        return ResponseEntity.ok().body(ApiResponse.success());
    }

    // 좋아요 취소
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> cancelPostLike(
            @AuthenticationPrincipal Long memberId,
            @PathVariable("postId") Long postId
    ) {
        postLikeService.cancelLike(memberId, postId);
        return ResponseEntity.ok().body(ApiResponse.success());
    }
}
