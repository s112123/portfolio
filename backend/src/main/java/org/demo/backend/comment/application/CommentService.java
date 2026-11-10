package org.demo.backend.comment.application;

import org.demo.backend.comment.application.dto.CommentDetail;
import org.demo.backend.comment.controller.dto.CommentCreateRequest;
import org.demo.backend.global.web.dto.PageDetail;

import java.util.List;

public interface CommentService {

    // 댓글 등록
    CommentDetail save(CommentCreateRequest request);

    // 댓글 목록
    PageDetail<CommentDetail> fetchByPostId(Long postId, int page, int size);

    // 댓글 삭제
    void deleteById(Long commentId);

    // 특정 게시물의 모든 댓글 삭제
    int deleteByPostIdIn(List<Long> postIds);
}
