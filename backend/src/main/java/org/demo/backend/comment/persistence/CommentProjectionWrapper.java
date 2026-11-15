package org.demo.backend.comment.persistence;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class CommentProjectionWrapper implements CommentProjection {

    private final CommentProjection projection;
    private final int replyCount;

    @Override
    public Long getId() {
        return projection.getId();
    }

    @Override
    public String getContent() {
        return projection.getContent();
    }

    @Override
    public LocalDateTime getRegisteredAt() {
        return projection.getRegisteredAt();
    }

    @Override
    public Long getParentId() {
        return projection.getParentId();
    }

    @Override
    public int getReplyCount() {
        return replyCount;
    }

    @Override
    public Long getWriterId() {
        return projection.getWriterId();
    }

    @Override
    public String getWriterNickname() {
        return projection.getWriterNickname();
    }

    @Override
    public String getWriterProfileImageUrl() {
        return projection.getWriterProfileImageUrl();
    }

    @Override
    public Long getPostWriterId() {
        return projection.getPostWriterId();
    }

    @Override
    public String getParentWriterNickname() {
        return projection.getParentWriterNickname();
    }
}
