package org.demo.backend.member.persistence;

import org.demo.backend.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {

    // 이메일이 존재하는지 여부
    boolean existsByEmail(String email);

    // 회원 조회
    Optional<Member> findByEmail(String email);
}
