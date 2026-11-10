package org.demo.backend.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.comment.application.CommentService;
import org.demo.backend.comment.application.dto.CommentDetail;
import org.demo.backend.comment.controller.dto.CommentCreateRequest;
import org.demo.backend.global.web.dto.ApiResponse;
import org.demo.backend.global.web.dto.PageDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 등록
    @PostMapping
    public ResponseEntity<ApiResponse<CommentDetail>> registerComment(
            @Valid @RequestBody CommentCreateRequest request
    ) {
        CommentDetail commentDetail = commentService.save(request);
        URI location = URI.create(String.format("/api/v1/comments/%d", commentDetail.getId()));
        return ResponseEntity.created(location).body(ApiResponse.success(commentDetail));
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable("commentId") Long commentId
    ) {
        commentService.deleteById(commentId);
        return ResponseEntity.noContent().build();
    }

    // 댓글 목록
    @GetMapping
    public ResponseEntity<ApiResponse<PageDetail<CommentDetail>>> readAllByPostId(
            @RequestParam(value = "postId") Long postId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page
    ) {
        PageDetail<CommentDetail> commentDetails =
                commentService.fetchByPostId(postId, (page < 1) ? 1 : page, 5);
        return ResponseEntity.ok().body(ApiResponse.success(commentDetails));
    }
}
