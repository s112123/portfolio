package org.demo.backend.global.file;

import lombok.extern.slf4j.Slf4j;
import org.demo.backend.global.file.dto.FileDetail;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
public class FileUtils {

    // 파일 저장
    public static FileDetail uploadFile(MultipartFile uploadFile, String uploadDirPath) {
        // 1. 파일 첨부 여부
        if (uploadFile == null || uploadFile.isEmpty()) {
            throw new IllegalArgumentException("파일이 첨부되지 않았습니다");
        }

        // 2. 원본 파일명과 확장자 추출
        String originalFilename = StringUtils.cleanPath(uploadFile.getOriginalFilename());
        String extension = extractFileExtension(originalFilename);

        // 3. 업로드 디렉토리 생성
        createUploadDirectory(uploadDirPath);

        // 4. 파일 업로드
        String storedFilename = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
        Path targetPath = Paths.get(uploadDirPath).resolve(storedFilename);
        try {
            uploadFile.transferTo(targetPath.toFile());
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", originalFilename, e);
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다", e);
        }

        return FileDetail.builder()
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .FilePath(targetPath.toString())
                .build();
    }

    // 파일 삭제
    public static void deleteFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return;
        }
        try {
            Path path = Paths.get(filePath);
            boolean deleted = Files.deleteIfExists(path);

            if (deleted) {
                log.info("물리 파일 삭제 성공: {}", filePath);
            } else {
                log.info("삭제할 물리 파일이 존재하지 않습니다: {}", filePath);
            }
        } catch (IOException e) {
            log.error("물리 파일 삭제 중 오류 발생: {}", filePath, e);
            throw new RuntimeException("물리 파일 삭제에 실패했습니다", e);
        }
    }

    // 파일 업로드 디렉토리 생성
    private static File createUploadDirectory(String uploadDirPath) {
        File uploadDir = new File(uploadDirPath);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (!created) {
                log.error("업로드 디렉토리 생성 실패: {}", uploadDir);
                throw new RuntimeException("서버의 파일 디렉토리를 생성할 수 없습니다");
            }
        }
        return uploadDir;
    }

    // 파일 확장자 추출
    private static String extractFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
}
