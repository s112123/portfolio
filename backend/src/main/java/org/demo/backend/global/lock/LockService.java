package org.demo.backend.global.lock;

import java.util.function.Supplier;

public interface LockService {

    <T> T executeWithLock(String lockKey, Supplier<T> action);
}
