package org.demo.backend.post.application;

import lombok.RequiredArgsConstructor;
import org.demo.backend.global.exception.BusinessException;
import org.demo.backend.global.file.FileUtils;
import org.demo.backend.global.file.dto.FileDetail;
import org.demo.backend.global.file.dto.FileUploadRequest;
import org.demo.backend.post.application.dto.PostImageDetail;
import org.demo.backend.post.domain.PostImage;
import org.demo.backend.post.domain.PostImageErrorCode;
import org.demo.backend.post.domain.PostImageProperties;
import org.demo.backend.post.domain.PostImageStatus;
import org.demo.backend.post.persistence.PostImageRepositoryAdapter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class PostImageJpaService implements PostImageService{

    private final PostImageProperties postImageProperties;
    private final ResourceLoader resourceLoader;
    private final PostImageRepositoryAdapter postImageRepositoryAdapter;

    @Override
    @Transactional
    public PostImageDetail uploadPostImage(FileUploadRequest request) {
        // 1. 서버에 파일 저장
        String uploadDirPath = postImageProperties.getStorage();
        FileDetail fileDetail = FileUtils.uploadFile(request.getFile(), uploadDirPath);
        // 2. DB 에 파일 정보 저장
        PostImage postImage = PostImage.of(
                fileDetail.getOriginalFilename(),
                fileDetail.getStoredFilename(),
                fileDetail.getFilePath(),
                postImageProperties.getImageUrl(fileDetail.getStoredFilename()),
                PostImageStatus.TEMP
        );
        return PostImageDetail.of(postImageRepositoryAdapter.save(postImage));
    }

    @Override
    public InputStream getPostImageInputStream(String imageFilename) {
        // 이미지 입력 스트림 반환
        try {
            Resource resource = getImageFileResource(imageFilename);
            if (resource.exists() || resource.isReadable()) {
                return resource.getInputStream();
            } else {
                throw new BusinessException(PostImageErrorCode.NOT_FOUND);
            }
        } catch (IOException e) {
            throw new RuntimeException("파일 스트림을 여는 중 오류가 발생했습니다:" + imageFilename, e);
        }
    }

    // 리소스 추출
    private Resource getImageFileResource(String filename) throws IOException {
        // 1. 기본 이미지인 경우 → classpath 에 있는 파일은 ResourceLoader 로 처리한다
        if (postImageProperties.getDefaultImage().getFile().equals(filename)) {
            String classpathLocation = postImageProperties.getDefaultImage().getPath() + filename;
            return resourceLoader.getResource(classpathLocation);
        }
        // 2. 일반 업로드 파일인 경우 (외부 스토리지)
        Path filePath = Paths.get(postImageProperties.getStorage(), filename);
        return new UrlResource(filePath.toUri());
    }
}
