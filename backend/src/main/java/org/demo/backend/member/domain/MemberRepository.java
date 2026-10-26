package org.demo.backend.member.domain;

import java.util.Optional;

public interface MemberRepository {

    // 회원 등록
    Member save(Member member);

    // 이메일이 존재하는지 여부
    boolean existsByEmail(String email);

    // 회원 조회
    Optional<Member> findById(Long memberId);

    // 회원 삭제
    void deleteById(Long memberId);
}
