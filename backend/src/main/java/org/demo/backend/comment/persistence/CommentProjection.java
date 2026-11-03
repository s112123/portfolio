package org.demo.backend.comment.persistence;

import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

public interface CommentProjection {

    // CommentDetail 스펙과 맞춘다
    @Value("#{target.id}")
    Long getId();
    @Value("#{target.content}")
    String getContent();
    @Value("#{target.registered_at}")
    LocalDateTime getRegisteredAt();
    @Value("#{target.parent_id}")
    Long getParentId();

    // 작성자 (Member) 정보 명시적 매핑
    @Value("#{target.writer_id}")
    Long getWriterId();
    @Value("#{target.writer_nickname}")
    String getWriterNickname();
    @Value("#{target.writer_profile_image_url}")
    String getWriterProfileImageUrl();

    // 게시글 작성자 판단용
    @Value("#{target.post_writer_id}")
    Long getPostWriterId();
    @Value("#{target.parent_writer_nickname}")
    String getParentWriterNickname();
}
