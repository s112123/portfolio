package org.demo.backend.comment.domain;

import org.demo.backend.comment.persistence.CommentProjection;
import org.demo.backend.global.api.dto.PageResult;

import java.util.Optional;

public interface CommentRepository {

    // 댓글 등록
    Comment save(Comment comment);
    
    // 댓글 조회
    Optional<Comment> findById(Long commentId);
    
    // 댓글 삭제
    void delete(Comment comment);
    
    // 댓글 목록
    PageResult<CommentProjection> fetchAllByPostId(Long postId, int page, int size);
}
