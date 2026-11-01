package org.demo.backend.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.demo.backend.post.domain.PostHitsCacheRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostHitsScheduler {

    private final PostHitsCacheRepository postHitsCacheRepository;
    private final PostHitsBatchRepository postHitsBatchRepository;

    // 1분마다 조회 수를 DB 에 업데이트 한다
    @Scheduled(fixedDelay = 60000)
    @SchedulerLock(
            name = "flushPostHitsToRdb",
            lockAtLeastFor = "0s",
            lockAtMostFor = "5m"
    )
    public void flushPostHitsToRdb() {
        // 1. 조회수 증분 캐시 조회
        Map<Long, Long> postHitsDelta = postHitsCacheRepository.getAllHitsDeltaAndClear();
        if (postHitsDelta.isEmpty()) {
            return;
        }

        // 2. 기존 조회 수에 원자적 배치 업데이트 (청크)
        int chunkSize = 500;
        ArrayList<Map.Entry<Long, Long>> entries = new ArrayList<>(postHitsDelta.entrySet());

        int successCount = 0;
        int failureCount = 0;
        for (int i = 0; i < entries.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, entries.size());
            List<Map.Entry<Long, Long>> chunkEntries = entries.subList(i, end);
            Map<Long, Long> chunkMap = chunkEntries.stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            try {
                // DB 업데이트
                postHitsBatchRepository.updateAllPostHits(chunkMap);
                successCount += chunkMap.size();
            } catch (Exception e) {
                failureCount += chunkMap.size();
                log.error("조회수 DB 업데이트 실패. 대상 게시글 수: {}", chunkMap.size(), e);

                // 실패한 조회 수는 Redis 캐시에 복구
                try {
                    postHitsCacheRepository.restoreHitsDelta(chunkMap);
                } catch (Exception restoreException) {
                    // 로그만 남김 → 조회 수는 약간 부정확해도 되므로 복구에 실패한 조회 수는 유실시키는 정책으로 한다
                    log.error("조회수 캐시 복구 실패. 유실된 게시글 수: {}", chunkMap.size(), restoreException);
                }
            }
        }

        log.info(
                "게시글 조회수 DB 반영 완료. 전체: {}, 성공: {}, 실패: {}",
                postHitsDelta.size(),
                successCount,
                failureCount
        );
    }
}
