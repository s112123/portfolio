package org.demo.backend.global.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PostHitsBatchRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    // 조회 수 업데이트 (배치)
    @Transactional
    public void updateAllPostHits(Map<Long, Long> hitsDeltaMap) {
        if (hitsDeltaMap == null || hitsDeltaMap.isEmpty()) {
            return;
        }

        SqlParameterSource[] batchParams = hitsDeltaMap.entrySet().stream()
                .map(entry -> new MapSqlParameterSource()
                        .addValue("postId", entry.getKey())
                        .addValue("delta", entry.getValue()))
                .toArray(size -> new SqlParameterSource[size]);

        String sql = "UPDATE post SET hits = hits + :delta WHERE id = :postId";
        jdbcTemplate.batchUpdate(sql, batchParams);
    }
}
