package org.demo.backend.comment.persistence;

import lombok.RequiredArgsConstructor;
import org.demo.backend.comment.domain.Comment;
import org.demo.backend.comment.domain.CommentRepository;
import org.demo.backend.global.web.dto.CursorDetail;
import org.demo.backend.global.web.dto.PageDetail;
import org.demo.backend.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryAdapter implements CommentRepository {

    private final CommentJpaRepository commentJpaRepository;

    @Override
    public Comment save(Comment comment) {
        return commentJpaRepository.save(comment);
    }

    @Override
    public Optional<Comment> findById(Long commentId) {
        return commentJpaRepository.findById(commentId);
    }

    @Override
    public void delete(Comment comment) {
        commentJpaRepository.delete(comment);
    }

    @Override
    public PageDetail<CommentProjection> fetchPageByPostId(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of((page - 1), size);
        Page<CommentProjection> commentProjections = commentJpaRepository.fetchPageByPostId(postId, pageable);
        return PageDetail.<CommentProjection>builder()
                .content(commentProjections.getContent())
                .currentPage(page)
                .totalPages(commentProjections.getTotalPages())
                .hasNext(commentProjections.hasNext())
                .hasPrevious(commentProjections.hasPrevious())
                .totalElements(commentProjections.getTotalElements())
                .size(size)
                .build();
    }

    @Override
    public int deleteByPostIdIn(List<Long> postIds) {
        return commentJpaRepository.deleteByPostIdIn(postIds);
    }

    @Override
    public int deleteByIdIn(List<Long> ids) {
        return commentJpaRepository.deleteByIdIn(ids);
    }

    @Override
    public CursorDetail<Comment> findCursorByMemberIdAndIdLessThan(Long memberId, Long lastCommentId, int size) {
        Pageable pageable = PageRequest.of(0, size);
        Slice<Comment> sliceComments =
                commentJpaRepository.findCursorByMemberIdAndIdLessThan(memberId, lastCommentId, pageable);
        return CursorDetail.<Comment>builder()
                .content(sliceComments.getContent())
                .hasNext(sliceComments.hasNext())
                .build();
    }
}
