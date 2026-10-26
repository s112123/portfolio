package org.demo.backend.otp.persistence;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.otp.domain.OtpRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;


@Slf4j
@Profile({"default", "test"})
@Repository
public class CaffeineOtpRepository implements OtpRepository {

    private final Cache<String, String> otpCache;

    public CaffeineOtpRepository(@Qualifier("otpCache") Cache<String, String> otpCache) {
        this.otpCache = otpCache;
    }

    @Override
    public void set(String identifier, String data) {
        otpCache.put(identifier, data);
    }

    @Override
    public String get(String identifier) {
        return otpCache.getIfPresent(identifier);
    }

    @Override
    public void invalidate(String identifier) {
        otpCache.invalidate(identifier);
    }
}
