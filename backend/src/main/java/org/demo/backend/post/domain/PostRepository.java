package org.demo.backend.post.domain;

import java.util.Optional;

public interface PostRepository {

    // 글 등록
    Post save(Post post);

    // 글 조회
    Optional<Post> fetchById(Long postId);

    // 글 수정

    // 글 삭제

    // 글 목록 → 정렬

    // 글 검색
}
