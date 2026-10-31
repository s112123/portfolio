package org.demo.backend.member.persistence;

import org.demo.backend.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {

    // 이메일이 존재하는지 여부
    boolean existsByEmail(String email);

    // 회원 조회
    @Query(value = "SELECT m FROM Member m LEFT JOIN FETCH m.profileImage WHERE m.email = :email")
    Optional<Member> findByEmail(@Param("email") String email);
}
