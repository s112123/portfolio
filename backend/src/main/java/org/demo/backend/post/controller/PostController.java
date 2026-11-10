package org.demo.backend.post.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.global.web.dto.ApiResponse;
import org.demo.backend.global.web.dto.CursorDetail;
import org.demo.backend.like.application.PostLikeService;
import org.demo.backend.post.application.PostService;
import org.demo.backend.post.application.dto.PostDetail;
import org.demo.backend.post.controller.dto.PostCreateRequest;
import org.demo.backend.post.controller.dto.PostUpdateRequest;
import org.demo.backend.post.domain.SortType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostLikeService postLikeService;

    // 글 등록
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody PostCreateRequest request
    ) {
        Long savedPostId = postService.save(memberId, request);
        URI location = URI.create(String.format("/api/v1/posts/%d", savedPostId));
        return ResponseEntity.created(location).body(ApiResponse.success(savedPostId));
    }

    // 글 조회
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetail>> readById(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal Long memberId,
            HttpServletRequest request
    ) {
        // 1. 뷰어는 회원인 경우, memberId 로 하고 비회원인 경우 IP 로 한다
        String viewer = "ip:" + getClientIp(request);
        boolean liked = false;
        if (memberId != null) {
            viewer = "member:" + memberId;
            // 로그인 회원인 경우, 이 글을 좋아요 했는지 여부 확인
            liked = postLikeService.existsByMemberIdAndPostId(memberId, postId);
        }

        // 3. 글 조회 및 조회 수 캐시
        PostDetail postDetail = postService.fetchByIdAndIncrementHitsCache(postId, viewer, liked);
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

    // 글 목록 (무한 스크롤)
    @GetMapping
    public ResponseEntity<ApiResponse<CursorDetail<PostDetail>>> readAllInfiniteScroll(
            @RequestParam(value = "sortType") SortType sortType,
            @RequestParam(value = "lastPostId", required = false) Long lastPostId,
            @RequestParam(value = "lastValue", required = false) Long lastValue,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        CursorDetail<PostDetail> postDetails =
                postService.fetchCursorById(sortType, lastPostId, lastValue, 10, keyword);
        return ResponseEntity.ok().body(ApiResponse.success(postDetails));
    }

    // 글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal Long memberId
    ) {
        postService.deleteById(postId, memberId);
        return ResponseEntity.ok().body(ApiResponse.success());
    }

    // IP 추출
    private String getClientIp(HttpServletRequest request) {
        // 프록시 서버에서 온 요청인 경우, 백엔드는 프록시 서버의 IP 만 알 수 있다
        // 그래서 프록시 서버인지 관련 헤더를 검사한다
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
