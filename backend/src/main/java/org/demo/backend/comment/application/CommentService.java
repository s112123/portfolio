package org.demo.backend.comment.application;

import org.demo.backend.comment.application.dto.CommentDetail;
import org.demo.backend.comment.controller.dto.CommentCreateRequest;
import org.demo.backend.global.api.dto.PageResult;

public interface CommentService {

    // 댓글 등록
    CommentDetail create(CommentCreateRequest request);

    // 댓글 삭제
    void deleteById(Long commentId);

    // 댓글 목록
    PageResult<CommentDetail> readAllByPostId(Long postId, int page, int size);
}
