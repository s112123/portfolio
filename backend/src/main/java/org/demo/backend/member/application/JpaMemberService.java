package org.demo.backend.member.application;

import lombok.RequiredArgsConstructor;
import org.demo.backend.global.exception.BusinessException;
import org.demo.backend.member.application.dto.MemberDetail;
import org.demo.backend.member.controller.dto.MemberCreateRequest;
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

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JpaMemberService implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
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
                        NicknameUtils.generate(),
                        ProfileImageProperties.DEFAULT_PROFILE_IMAGE,
                        ProfileImageProperties.DEFAULT_PROFILE_IMAGE_URL,
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
}
