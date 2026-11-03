package org.demo.backend.member.application;

import lombok.RequiredArgsConstructor;
import org.demo.backend.global.exception.BusinessException;
import org.demo.backend.member.application.dto.MemberDetail;
import org.demo.backend.member.controller.dto.MemberCreateRequest;
import org.demo.backend.member.controller.dto.PasswordChangeRequest;
import org.demo.backend.member.domain.Member;
import org.demo.backend.member.domain.MemberErrorCode;
import org.demo.backend.member.domain.MemberRepository;
import org.demo.backend.member.domain.ProfileImageProperties;
import org.demo.backend.otp.application.OtpService;
import org.demo.backend.otp.domain.OtpErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberJpaService implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileImageProperties profileImageProperties;
    private final OtpService otpService;

    // 회원 등록
    @Override
    @Transactional
    public MemberDetail create(MemberCreateRequest request) {
        // 1. 인증 코드 검증 상태 확인
        if (!otpService.isVerified(request.getEmail())) {
            throw new BusinessException(OtpErrorCode.NOT_VERIFIED_CODE);
        }
        // 2. 존재하는 이메일인지 여부
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(MemberErrorCode.DUPLICATED_EMAIL);
        }
        // 3. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        // 4. 회원 등록
        Member savedMember = memberRepository.save(
                Member.of(
                        request.getEmail(),
                        encodedPassword,
                        generateUniqueNickname(),
                        profileImageProperties.getDefaultImageUrl(),
                        LocalDateTime.now()
                )
        );
        return MemberDetail.of(savedMember);
    }

    @Override
    public MemberDetail readById(Long memberId) {
        // 회원 조회
        Member foundMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND));
        return MemberDetail.of(foundMember);
    }

    @Override
    public void deleteById(Long memberId) {
        // 1. 회원 조회
        Member foundMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND));
        // 2. 파일 삭제
        // 3. 회원 삭제
        memberRepository.deleteById(foundMember.getId());
    }

    @Override
    @Transactional
    public String changePassword(PasswordChangeRequest request) {
        String rePassword = "1234";
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND));
        member.updatePassword(passwordEncoder.encode(rePassword));
        return rePassword;
    }

    // 고유한 닉네임 생성
    private String generateUniqueNickname() {
        final int MAX_RETRY_COUNT = 5;
        for (int i = 0; i < MAX_RETRY_COUNT; i++) {
            String candidate = NicknameUtils.generate();
            if (!memberRepository.existsByNickname(candidate)) {
                return candidate;
            }
        }
        // 5번 생성 시도에도 닉네임이 중복되면 1~9999의 숫자를 붙인다
        String baseNickname = NicknameUtils.generate();
        int randomNum = ThreadLocalRandom.current().nextInt(1, 10000);
        return baseNickname + "-" + randomNum;
    }
}
