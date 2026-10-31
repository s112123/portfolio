package org.demo.backend.global.scheduler;

import lombok.RequiredArgsConstructor;
import org.demo.backend.global.file.FileUtils;
import org.demo.backend.post.domain.PostImage;
import org.demo.backend.post.domain.PostImageStatus;
import org.demo.backend.post.persistence.PostImageRepositoryAdapter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FileCleanupScheduler {

    private final PostImageRepositoryAdapter postImageRepositoryAdapter;

    // 임시 파일 삭제 (매일 새벽 3시마다)
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanUpOrphanPostImages() {
        // 1. 파일 상태가 TEMP 이면서 24시간이 지난 파일 조회
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<PostImage> orphanImages = postImageRepositoryAdapter.findByStatusAndCreatedAtBefore(
                PostImageStatus.TEMP,
                threshold
        );

        // 2. 파일 삭제
        if (!orphanImages.isEmpty()) {
            // 서버의 물리 파일 삭제
            for (PostImage orphanImage : orphanImages) {
                FileUtils.deleteFile(orphanImage.getFilePath());
            }
            // DB 레코드 일괄 삭제
            postImageRepositoryAdapter.deleteAllInBatch(orphanImages);
        }
    }
}
