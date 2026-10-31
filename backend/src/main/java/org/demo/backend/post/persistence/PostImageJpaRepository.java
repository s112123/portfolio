package org.demo.backend.post.persistence;

import org.demo.backend.post.domain.Post;
import org.demo.backend.post.domain.PostImage;
import org.demo.backend.post.domain.PostImageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostImageJpaRepository extends JpaRepository<PostImage, Long> {

    // 파일 조회
    Optional<PostImage> findByStoredFilename(String storedFilename);

    // 이미지 등록 → FK 를 설정하고 상태를 Attached 로 한다
    @Modifying(flushAutomatically = true)
    @Query(value = "" +
            "UPDATE PostImage pi " +
            "   SET pi.post = :post, " +
            "       pi.status = 'ATTACHED' " +
            "WHERE pi.storedFilename IN :filenames")
    int attachImages(
            @Param("post") Post post,
            @Param("filenames") List<String> filenames
    );

    // 이미지 제거 → FK 를 NULL 로 만들고 상태를 TEMP 로 한다
    @Modifying(flushAutomatically = true)
    @Query(value = "" +
            "UPDATE PostImage pi " +
            "   SET pi.post = NULL, " +
            "       pi.status = 'TEMP' " +
            "WHERE pi.storedFilename IN :filenames")
    int detachImages(
            @Param("filenames") List<String> filenames
    );

    // 글 이미지 파일 목록 (status = :stats and created_At < :threshold)
    List<PostImage> findByStatusAndCreatedAtBefore(PostImageStatus status, LocalDateTime threshold);
}
