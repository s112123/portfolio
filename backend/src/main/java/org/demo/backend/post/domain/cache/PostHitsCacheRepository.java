package org.demo.backend.post.domain.cache;

import java.util.Map;

public interface PostHitsCacheRepository {

    // 뷰어를 저장하고 게시물 조회 수 증분을 저장한다
    Long addViewerAndIncrementDelta(Long postId, String viewer);

    // 조회 수의 모든 증분값 조회
    Map<Long, Long> findHitsDeltaAndClear();

    // 업데이트 중 실패한 조회 수를 다시 복구
    void restoreHitsDelta(Map<Long, Long> postHitsDelta);

    // 조회 수 캐시 삭제
    void deleteHits(Long postId);

    // 뷰어 캐시 삭제
    void deleteViewer(Long postId);
}
