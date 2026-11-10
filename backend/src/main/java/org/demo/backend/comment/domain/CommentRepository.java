package org.demo.backend.comment.domain;

import org.demo.backend.comment.persistence.CommentProjection;
import org.demo.backend.global.web.dto.PageDetail;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    // 댓글 등록
    Comment save(Comment comment);
    
    // 댓글 조회
    Optional<Comment> findById(Long commentId);
    
    // 댓글 삭제
    void delete(Comment comment);
    
    // 댓글 목록
    PageDetail<CommentProjection> fetchPageByPostId(Long postId, int page, int size);

    // 특정 게시물의 모든 댓글 삭제
    int deleteByPostIdIn(List<Long> postIds);
}
