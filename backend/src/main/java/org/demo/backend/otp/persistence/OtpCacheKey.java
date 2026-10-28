package org.demo.backend.otp.persistence;

public abstract class OtpCacheKey {

    // OTP 코드를 저장하는 키
    private static final String OTP_PREFIX = "otp:";

    // OTP 코드 검증 성공 여부를 저장하는 키
    private static final String OTP_VERIFIED_PREFIX = "otp:verified:";

    // OTP 코드를 저장하는 키
    public static String generateOtpKey(String identifier) {
        return OTP_PREFIX + identifier;
    }

    // OTP 코드 검증 결과를 저장하는 키
    public static String generateVerifiedKey(String identifier) {
        return OTP_VERIFIED_PREFIX + identifier;
    }
}
