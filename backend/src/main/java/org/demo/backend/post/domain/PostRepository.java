package org.demo.backend.post.domain;

import org.demo.backend.global.web.dto.CursorDetail;
import org.demo.backend.global.web.dto.PageDetail;

import java.util.List;
import java.util.Optional;

public interface PostRepository {

    // 글 등록
    Post save(Post post);

    // 글 조회
    Optional<Post> findById(Long postId);

    // 글 조회
    Optional<Post> fetchById(Long postId);

    // 좋아요 수 +1 증가
    int incrementLikeById(Long postId);

    // 좋아요 수 -1 감소
    int decrementLikeById(Long postId);

    // 글 목록 (무한 스크롤)
    CursorDetail<Post> fetchCursorByIdLessThan(
            SortType sortType,
            Long lastPostId,
            Long lastValue,
            int size,
            String keyword
    );

    // 글 목록 (IN postIds)
    List<Post> fetchByIdIn(List<Long> postIds);

    // 특정 회원의 글 목록 (Pagination)
    PageDetail<Post> fetchPageByMemberId(
            Long memberId,
            int page,
            int size
    );

    // 특정 회원의 글 목록 (Cursor)
    CursorDetail<Post> findCursorByIdLessThenAndMemberId(
            Long memberId,
            Long lastPostId,
            int size
    );

    // 글 삭제
    void delete(Post post);

    // 글 삭제 (배치)
    int deleteByIdIn(List<Long> ids);
}
