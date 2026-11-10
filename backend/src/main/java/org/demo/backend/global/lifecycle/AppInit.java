package org.demo.backend.global.lifecycle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.member.domain.Member;
import org.demo.backend.member.domain.MemberRepository;
import org.demo.backend.member.domain.MemberRole;
import org.demo.backend.member.domain.ProfileImageProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppInit {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileImageProperties profileImageProperties;

    @PostConstruct
    @Transactional
    public void init() {
        // 최고 관리자 등록
        String rootEmail = "root@test.com";
        if (!memberRepository.existsByEmail(rootEmail)) {
            Member root = Member.of(
                    "root@test.com",
                    passwordEncoder.encode("1234"),
                    "최고 관리자",
                    profileImageProperties.getDefaultImageUrl(),
                    LocalDateTime.now()
            );
            root.updateRole(MemberRole.ROOT);
            memberRepository.save(root);
        }

        // 관리자 등록
        String adminEmail = "admin@test.com";
        if (!memberRepository.existsByEmail(adminEmail)) {
            Member admin = Member.of(
                    adminEmail,
                    passwordEncoder.encode("1234"),
                    "관리자",
                    profileImageProperties.getDefaultImageUrl(),
                    LocalDateTime.now()
            );
            admin.updateRole(MemberRole.ADMIN);
            memberRepository.save(admin);
        }

        log.info("[Init] 애플리케이션 데이터 초기화 완료");
    }
}
