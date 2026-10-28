package org.demo.backend.global.lock;

import lombok.RequiredArgsConstructor;
import org.demo.backend.global.exception.BusinessException;
import org.demo.backend.global.jwt.domain.JwtErrorCode;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Profile({"default", "test"})
@Component
@RequiredArgsConstructor
public class LocalLockService implements LockService {

    private final ConcurrentMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    @Override
    public <T> T executeWithLock(String lockKey, Supplier<T> action) {
        ReentrantLock lock = lockMap.computeIfAbsent(lockKey, k -> new ReentrantLock());
        boolean available = false;
        try {
            available = lock.tryLock(3, TimeUnit.SECONDS);
            if (!available) {
                throw new BusinessException(LockErrorCode.CURRENT_REQUEST_BLOCKED);
            }
            return action.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(LockErrorCode.CURRENT_REQUEST_BLOCKED);
        } finally {
            if (available && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
