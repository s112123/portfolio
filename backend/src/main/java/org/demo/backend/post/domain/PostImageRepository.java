package org.demo.backend.post.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostImageRepository {

    // 글 이미지 파일 정보 저장
    PostImage save(PostImage postImage);

    // 글 이미지 파일 조회
    Optional<PostImage> findByStoredFilename(String storedFilename);

    // 글 수정 시, 새로운 이미지 추가
    int attachImages(Post post, List<String> filenames);

    // 글 수정 시, 기존 이미지 제거
    int detachImages(List<String> filenames);

    // 글 이미지 목록 (파일 상태와 특정 시간 미만)
    List<PostImage> findByStatusAndCreatedAtBefore(PostImageStatus status, LocalDateTime threshold);

    // 글 이미지 일괄 삭제
    void deleteAllInBatch(List<PostImage> postImages);
}
