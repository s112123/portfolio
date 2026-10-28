package org.demo.backend.otp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.global.api.dto.ApiResponse;
import org.demo.backend.global.exception.BusinessException;
import org.demo.backend.global.sender.MessageSender;
import org.demo.backend.member.domain.MemberErrorCode;
import org.demo.backend.member.domain.MemberRepository;
import org.demo.backend.otp.application.OtpService;
import org.demo.backend.otp.controller.dto.EmailCodeCreateRequest;
import org.demo.backend.otp.controller.dto.EmailCodeVerifyRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;
    private final MessageSender messageSender;
    private final MemberRepository memberRepository;

    // 이메일 인증 코드 발송
    @PostMapping("/send/email")
    public ResponseEntity<ApiResponse<?>> sendEmailCode(
            @Valid @RequestBody EmailCodeCreateRequest request
    ) {
        // 1. 중복 이메일 검사
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(MemberErrorCode.DUPLICATED_EMAIL);
        }
        // 2. 인증 코드 생성
        String authCode = otpService.generate(request.getEmail());
        // 3. 인증 코드 전송 (동기 전송)
        // 비동기 전송 시, 인증 코드가 발송되지 않았는데 사용자에게는 발송되었다고 빠른 응답할 수 있다
        messageSender.send(request.getEmail(), authCode);
        return ResponseEntity.ok().body(ApiResponse.success(authCode));
    }

    // 이메일 인증 코드 검증
    @PostMapping("/verify/email")
    public ResponseEntity<ApiResponse<?>> verifyEmailCode(
            @Valid @RequestBody EmailCodeVerifyRequest request
    ) {
        // 1. 인증 코드 검증
        otpService.verify(request.getEmail(), request.getCode());
        return ResponseEntity.ok().body(ApiResponse.success());
    }
}
