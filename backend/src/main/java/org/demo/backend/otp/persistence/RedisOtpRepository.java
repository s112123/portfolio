package org.demo.backend.otp.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.otp.domain.OtpRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Slf4j
@Profile({"dev", "prod"})
@Repository
@RequiredArgsConstructor
public class RedisOtpRepository implements OtpRepository {

    private final String OTP_KEY_FORMAT = "otp:code:%s";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void set(String identifier, String data) {
        String key = String.format(OTP_KEY_FORMAT, identifier);
        redisTemplate.opsForValue().set(key, data, Duration.ofMinutes(3));
    }

    @Override
    public String get(String identifier) {
        String key = String.format(OTP_KEY_FORMAT, identifier);
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void invalidate(String identifier) {
        String key = String.format(OTP_KEY_FORMAT, identifier);
        redisTemplate.delete(key);
    }
}
