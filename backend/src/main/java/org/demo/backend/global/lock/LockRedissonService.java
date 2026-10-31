package org.demo.backend.global.lock;

import lombok.RequiredArgsConstructor;
import org.demo.backend.global.exception.BusinessException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Profile({"dev", "prod"})
@Component
@RequiredArgsConstructor
public class LockRedissonService implements LockService {

    private final RedissonClient redissonClient;

    @Override
    public <T> T executeWithLock(String lockKey, Supplier<T> action) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 1. 락 획득 시도
            // waitTime → 3초 대기 시간 / leasTime: 5초 동안 유지 후 자동 해제
            boolean available = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if (!available) {
                throw new BusinessException(LockErrorCode.CURRENT_REQUEST_BLOCKED);
            }
            // 2. 락 획득 시, 작업 실행
            return action.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(LockErrorCode.CURRENT_REQUEST_BLOCKED);
        } finally {
            // 락 자원 해제
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
