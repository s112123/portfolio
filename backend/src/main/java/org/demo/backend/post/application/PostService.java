package org.demo.backend.post.application;

import org.demo.backend.global.api.dto.PageResult;
import org.demo.backend.post.application.dto.PostDetail;
import org.demo.backend.post.controller.dto.PostCreateRequest;
import org.demo.backend.post.controller.dto.PostUpdateRequest;
import org.demo.backend.post.domain.SortType;

public interface PostService {

    // 글 등록
    Long create(Long writerId, PostCreateRequest request);

    // 글 조회 + 조회 수 캐시
    PostDetail readByIdWithHitsCache(Long postId, String viewer, boolean liked);

    // 글 수정
    void update(Long memberId, Long postId, PostUpdateRequest request);

    // 전체 글 목록
    PageResult<PostDetail> findAllInfiniteScroll(
            SortType sortType,
            Long lastPostId,
            Long lastValue,
            int size,
            String keyword
    );

    // 특정 회원의 글 목록
    PageResult<PostDetail> findAllByMemberId(Long memberId, int page, int size);

    // 글 삭제
}
