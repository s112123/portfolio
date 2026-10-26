package org.demo.backend.otp.application;

import lombok.RequiredArgsConstructor;
import org.demo.backend.global.exception.BusinessException;
import org.demo.backend.otp.domain.OtpErrorCode;
import org.demo.backend.otp.domain.OtpRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;

    // 인증 코드 생성
    public String generate(String identifier) {
        // 1. 유효성 검사
        Assert.hasText(identifier, "identifier is not null or not blank");
        // 2. 인증 코드 6자리 생성
        String verifyCode = UUID.randomUUID().toString().substring(0, 6);
        // 3. 인증 코드를 캐시에 저장 (TTL: 3분)
        otpRepository.set(identifier, verifyCode);
        return verifyCode;
    }

    // 인증 코드 확인
    public void verify(String identifier, String code) {
        // 1. 유효성 검사
        Assert.hasText(identifier, "identifier is not null or not blank");
        Assert.hasText(code, "code is not null or not blank");
        // 2. 캐시에 저장된 인증 코드 조회 (없거나 만료되면 null 반환)
        String savedCode = otpRepository.get(identifier);
        // 3. 저장된 코드가 없거나 입력받은 코드와 일치하지 않는 경우
        if (savedCode == null || !savedCode.equals(code)) {
            throw new BusinessException(OtpErrorCode.INVALID_CODE);
        }
        // 4. 인증 성공 시, 재사용 방지를 위해 즉시 캐시에서 제거
        otpRepository.invalidate(identifier);
    }
}
