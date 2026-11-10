package org.demo.backend.comment.application;

import lombok.RequiredArgsConstructor;
import org.demo.backend.comment.application.dto.CommentDetail;
import org.demo.backend.comment.controller.dto.CommentCreateRequest;
import org.demo.backend.comment.domain.Comment;
import org.demo.backend.comment.domain.CommentErrorCode;
import org.demo.backend.comment.domain.CommentRepository;
import org.demo.backend.comment.persistence.CommentProjection;
import org.demo.backend.global.exception.BusinessException;
import org.demo.backend.global.web.dto.PageDetail;
import org.demo.backend.member.domain.Member;
import org.demo.backend.member.domain.MemberErrorCode;
import org.demo.backend.member.domain.MemberRepository;
import org.demo.backend.post.domain.Post;
import org.demo.backend.post.domain.PostErrorCode;
import org.demo.backend.post.domain.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentJpaService implements CommentService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public CommentDetail save(CommentCreateRequest request) {
        // 1. 게시물 조회
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new BusinessException(PostErrorCode.NOT_FOUND));
        // 2. 작성자 조회
        Member writer = memberRepository.findById(request.getWriterId())
                .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND));

        // 3. 댓글 저장
        Comment comment = Comment.of(writer, request.getContent());
        if (request.getParentId() != null) {
            commentRepository.findById(request.getParentId())
                    .ifPresent(parent -> comment.assignParent(parent));
        }
        post.addComment(comment);
        Comment savedComment = commentRepository.save(comment);
        return CommentDetail.of(savedComment);
    }

    @Override
    public PageDetail<CommentDetail> fetchByPostId(Long postId, int page, int size) {
        PageDetail<CommentProjection> projection = commentRepository.fetchPageByPostId(postId, page, size);
        return projection.map(p -> CommentDetail.ofCommentProject(p));
    }

    @Override
    @Transactional
    public void deleteById(Long commentId) {
        // 1. 댓글이 있는지 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));
        // 2. 댓글 삭제
        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public int deleteByPostIdIn(List<Long> postIds) {
        return commentRepository.deleteByPostIdIn(postIds);
    }
}
