package org.demo.backend.otp.domain;

public interface OtpRepository {

    // 캐시 저장
    void set(String identifier, String data);

    // 캐시 조회
    String get(String identifier);

    // 캐시 무효화
    void invalidate(String identifier);
}
