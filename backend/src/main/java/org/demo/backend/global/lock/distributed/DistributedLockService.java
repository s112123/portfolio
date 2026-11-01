package org.demo.backend.global.lock.distributed;

import java.util.function.Supplier;

public interface DistributedLockService {

    <T> T executeWithLock(String lockKey, Supplier<T> action);
}
