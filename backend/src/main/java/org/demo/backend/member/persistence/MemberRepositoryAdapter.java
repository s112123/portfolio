package org.demo.backend.member.persistence;

import lombok.RequiredArgsConstructor;
import org.demo.backend.member.domain.Member;
import org.demo.backend.member.domain.MemberRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryAdapter implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Member save(Member member) {
        return memberJpaRepository.save(member);
    }

    @Override
    public boolean existsByEmail(String email) {
        return memberJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByNickname(String Nickname) {
        return memberJpaRepository.existsByNickname(Nickname);
    }

    @Override
    public Optional<Member> findById(Long memberId) {
        return memberJpaRepository.findById(memberId);
    }

    @Override
    @Transactional
    public void deleteById(Long memberId) {
        memberJpaRepository.deleteById(memberId);
    }

    @Override
    public Optional<Member> fetchByEmail(String email) {
        return memberJpaRepository.fetchByEmail(email);
    }
}
