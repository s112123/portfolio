package org.demo.backend.global.lock.distributed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.global.exception.BusinessException;
import org.demo.backend.global.lock.LockErrorCode;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Profile({"dev", "prod"})
@Component
@RequiredArgsConstructor
public class DistributedLockLettuceService implements DistributedLockService {

    private final StringRedisTemplate redisTemplate;
    private static final RedisScript<Long> UNLOCK_SCRIPT = RedisScript.of(
            "if redis.call('GET', KEYS[1]) == ARGV[1] then " +
            "   return redis.call('DEL', KEYS[1]) " +
            "else " +
            "   return 0 " +
            "end",
            Long.class
    );

    @Override
    public <T> T executeWithLock(String lockKey, Supplier<T> action) {
        // 락 소유권을 증명할 고유 값
        String lockValue = UUID.randomUUID().toString();
        // 3초 동안 락 획득 시도
        long waitTimeMillis = 3000;
        // 5초 후 자동 만료 (데드락 방지)
        long leaseTimeMillis = 5000;
        long startTime = System.currentTimeMillis();

        boolean acquired = false;
        try {
            // 1. 락 획득 시도 (Spin Lock 방식) → 대기 시간 동안 락 획득을 시도한다
            while ((System.currentTimeMillis() - startTime) < waitTimeMillis) {
                // SET key value NX PX timeout → Redis 2.6.12+ 지원
                Boolean result = redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, lockValue, Duration.ofMillis(leaseTimeMillis));
                acquired = Boolean.TRUE.equals(result);
                if (acquired){
                    break;
                }

                // 락 획득 실패 시 짧게 대기 후 재시도 (CPU 과부하 방지)
                TimeUnit.MILLISECONDS.sleep(100);
            }

            if (!acquired) {
                throw new BusinessException(LockErrorCode.CURRENT_REQUEST_BLOCKED);
            }

            // 2. 락 획득 시, 작업 실행
            return action.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(LockErrorCode.CURRENT_REQUEST_BLOCKED);
        } finally {
            // 3. 락 자원 해제
            if (acquired) {
                try {
                    redisTemplate.execute(
                            UNLOCK_SCRIPT,
                            Collections.singletonList(lockKey),
                            lockValue
                    );
                } catch (Exception e) {
                    // 락 해제 실패 시 로그 기록 (네트워크 이슈 등)
                    log.error("Failed to release distributed lock for key: {}", lockKey, e);
                }
            }
        }
    }
}
