package org.demo.backend.member.controller;

import lombok.extern.slf4j.Slf4j;
import org.demo.backend.common.security.WithMockCustomUser;
import org.demo.backend.global.security.SecurityConfig;
import org.demo.backend.member.application.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(MemberController.class)
@Import(SecurityConfig.class)
public class MemberDeleteMockTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MemberService memberService;

    @Test
    @DisplayName("회원 탈퇴: 성공")
    @WithMockCustomUser(memberId = 1L)
    void deleteMember_Success() throws Exception {
        // 1. deleteById() 모킹
        doNothing().when(memberService).deleteById(1L);
        // 2. 회원 탈퇴
        mockMvc.perform(delete("/api/v1/members"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("회원 탈퇴: 실패 - 로그인 되지 않은 경우, 401 응답을 반환한다")
    void deleteMember_Failure_NotAuthenticated() throws Exception {
        // 회원 탈퇴
        mockMvc.perform(delete("/api/v1/members"))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }
}
