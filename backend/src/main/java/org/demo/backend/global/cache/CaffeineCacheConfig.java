package org.demo.backend.global.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineCacheConfig {

    @Qualifier("otpCache")
    @Bean
    public Cache<String, String> otpCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(3, TimeUnit.MINUTES)
                .maximumSize(10)
                .build();
    }
}
