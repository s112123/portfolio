package org.demo.backend.post.application;

import org.demo.backend.global.web.dto.CursorDetail;
import org.demo.backend.global.web.dto.PageDetail;
import org.demo.backend.post.application.dto.PostDetail;
import org.demo.backend.post.controller.dto.PostCreateRequest;
import org.demo.backend.post.controller.dto.PostUpdateRequest;
import org.demo.backend.post.domain.SortType;

import java.util.List;

public interface PostService {

    // 글 등록
    Long save(Long writerId, PostCreateRequest request);

    // 글 조회 + 조회 수 캐시
    PostDetail fetchByIdAndIncrementHitsCache(Long postId, String viewer, boolean liked);

    // 글 수정
    void update(Long memberId, Long postId, PostUpdateRequest request);

    // 전체 글 목록
    CursorDetail<PostDetail> fetchCursorById(
            SortType sortType,
            Long lastPostId,
            Long lastValue,
            int size,
            String keyword
    );

    // 특정 회원의 글 목록
    PageDetail<PostDetail> fetchPageByMemberId(Long memberId, int page, int size);

    // 글 삭제
    void deleteById(Long postId, Long memberId);

    // 글 삭제 (배치)
    void deleteByIdIn(List<Long> postIds);
}
