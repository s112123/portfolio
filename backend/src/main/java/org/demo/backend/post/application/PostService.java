package org.demo.backend.post.application;

import org.demo.backend.post.application.dto.PostDetail;
import org.demo.backend.post.controller.dto.PostCreateRequest;
import org.demo.backend.post.controller.dto.PostUpdateRequest;

public interface PostService {

    // 글 등록
    Long create(Long writerId, PostCreateRequest request);

    // 글 조회
    PostDetail readById(Long postId);

    // 글 수정
    void update(Long memberId, Long postId, PostUpdateRequest request);

    // 글 삭제

    // 글 목록 → 정렬

    // 글 검색
}
