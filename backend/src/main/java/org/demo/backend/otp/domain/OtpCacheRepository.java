package org.demo.backend.otp.domain;

import java.time.Duration;

public interface OtpCacheRepository {

    // OTP 코드 저장
    void saveOtp(String identifier, String data, Duration ttl);

    // OTP 코드 조회
    String getOtp(String identifier);

    // OTP 코드 캐시 무효화
    void invalidateOpt(String identifier);

    // OTP 코드 검증
    void saveVerificationResult(String identifier, Duration ttl);

    // OTP 코드 검증 상태
    boolean isVerified(String identifier);
}
