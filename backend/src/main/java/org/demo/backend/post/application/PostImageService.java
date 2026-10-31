package org.demo.backend.post.application;

import org.demo.backend.global.file.dto.FileUploadRequest;
import org.demo.backend.post.application.dto.PostImageDetail;

import java.io.InputStream;

public interface PostImageService {

    // 글 이미지 파일 정보 저장
    PostImageDetail uploadPostImage(FileUploadRequest request);

    // 글 이미지 파일 입력 스트림 반환
    InputStream getPostImageInputStream(String imageFilename);
}
