package org.demo.backend.post.domain.cache;

public abstract class PostHitsCacheKey {

    // 조회 수 증분 값을 저장하는 키
    // 자료구조 → HASH
    // key → post:hits:delta
    // value → postId=delta
    // ttl → 없음
    private static final String POST_HITS_DELTA_KEY = "post:hits:delta";

    // 사용자별 조회 여부를 저장하는 키
    // 자료구조 → SET
    // key → post:viewers:{postId}
    // value → memberId 또는 IP
    // ttl → 24시간
    private static final String POST_VIEWERS_PREFIX = "post:viewers:";

    // 조회 수 증분 값을 저장하는 키
    public static String generatePostHitsKey() {
        return POST_HITS_DELTA_KEY;
    }

    // 사용자별 조회 여부를 저장하는 키
    public static String generatePostViewerKey(Long postId) {
        return POST_VIEWERS_PREFIX + postId;
    }
}
