package org.demo.backend.member.application;

import org.demo.backend.member.application.dto.MemberDetail;
import org.demo.backend.member.controller.dto.MemberCreateRequest;
import org.demo.backend.member.controller.dto.PasswordChangeRequest;
import org.demo.backend.member.domain.MemberRole;

public interface MemberService {

    // 회원 등록
    MemberDetail save(MemberCreateRequest request);

    // 회원 조회
    MemberDetail findById(Long memberId);

    // 회원 삭제
    void deleteById(Long memberId);

    // 비밀번호 변경
    String changePassword(PasswordChangeRequest request);

    // 권한 변경
    void changeRole(Long memberId, MemberRole role);
}
