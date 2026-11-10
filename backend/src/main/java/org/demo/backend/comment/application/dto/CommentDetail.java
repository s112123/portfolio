package org.demo.backend.comment.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.demo.backend.comment.domain.Comment;
import org.demo.backend.comment.persistence.CommentProjection;
import org.demo.backend.member.application.dto.MemberDetail;

import java.time.LocalDateTime;

@Getter
public class CommentDetail {

    private Long id;
    private MemberDetail writer;
    private String content;
    private LocalDateTime registeredAt;
    @JsonProperty("isPostAuthor")
    private boolean postAuthor;
    private Long parentId;
    private String parentWriterNickname;
    private int replyCount;

    public static CommentDetail of(Comment comment) {
        CommentDetail detail = new CommentDetail();
        detail.id = comment.getId();
        detail.writer = MemberDetail.of(comment.getWriter());
        detail.content = comment.getContent();
        detail.registeredAt = comment.getRegisteredAt();
        detail.postAuthor = comment.getWriter().getId().equals(
                comment.getPost().getMember().getId()
        );
        if (comment.getParent() != null) {
            detail.parentId = comment.getParent().getId();
            detail.parentWriterNickname = comment.getParent().getWriter().getNickname();
        }
        return detail;
    }

    public static CommentDetail ofCommentProject(CommentProjection projection) {
        CommentDetail detail = new CommentDetail();
        detail.id = projection.getId();
        detail.writer = MemberDetail.ofCommentProject(projection);
        detail.content = projection.getContent();
        detail.registeredAt = projection.getRegisteredAt();
        detail.postAuthor = projection.getWriterId().equals(projection.getPostWriterId());
        detail.parentId = projection.getParentId();
        detail.parentWriterNickname = projection.getParentWriterNickname();
        detail.replyCount = projection.getReplyCount();
        return detail;
    }
}
