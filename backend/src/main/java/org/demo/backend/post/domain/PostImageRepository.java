package org.demo.backend.post.domain;

import java.time.LocalDateTime;
import java.util.List;

public interface PostImageRepository {

    // 글 이미지 파일 정보 저장
    PostImage save(PostImage postImage);

    // 글 수정 시, 새로운 이미지 추가
    int attachImageByFilenameIn(Post post, List<String> filenames);

    // 글 수정 시, 기존 이미지 제거
    int detachImageByFilenameIn(List<String> filenames);

    // 글 수정 시, 기존 이미지 제거
    int detachImageByIdIn(List<Long> ids);

    // 글 이미지 목록 (파일 상태와 특정 시간 미만)
    List<PostImage> findByStatusAndCreatedAtBefore(PostImageStatus status, LocalDateTime threshold);

    // 글 이미지 일괄 삭제
    void deleteByPostImageIn(List<PostImage> postImages);
}
