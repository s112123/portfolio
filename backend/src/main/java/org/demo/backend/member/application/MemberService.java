package org.demo.backend.member.application;

import org.demo.backend.member.application.dto.MemberDetail;
import org.demo.backend.member.controller.dto.MemberCreateRequest;

public interface MemberService {

    // 회원 등록
    MemberDetail create(MemberCreateRequest request);

    // 회원 조회
    MemberDetail readById(Long memberId);

    // 회원 삭제
    void deleteById(Long memberId);
}
