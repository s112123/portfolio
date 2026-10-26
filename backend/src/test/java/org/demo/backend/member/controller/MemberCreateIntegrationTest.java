package org.demo.backend.member.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.common.IntegrationTest;
import org.demo.backend.member.application.dto.MemberDetail;
import org.demo.backend.member.controller.dto.MemberCreateRequest;
import org.demo.backend.member.domain.MemberRole;
import org.demo.backend.member.fixture.MemberFixture;
import org.demo.backend.otp.application.OtpService;
import org.demo.backend.otp.controller.dto.EmailCodeRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@IntegrationTest
class MemberCreateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OtpService otpService;

    @Test
    @DisplayName("회원 등록: 성공")
    void createMember_success() throws Exception {
        // 1. 사용자 정보
        MemberFixture memberFixture = MemberFixture.create();
        String email = memberFixture.getEmail();
        String password = memberFixture.getPassword();

        // 2. 인증 코드 발송
        EmailCodeRequest emailCodeRequest = new EmailCodeRequest(email);
        MvcResult emailCodeResult = mockMvc.perform(post("/api/v1/otp/send/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailCodeRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String emailCodeResponseBody = emailCodeResult.getResponse().getContentAsString();
        String authCode = objectMapper.readTree(emailCodeResponseBody).get("data").asText();

        // 3. 회원 등록
        MemberCreateRequest memberCreateRequest = new MemberCreateRequest(email, password, authCode);
        String requestBody = objectMapper.writeValueAsString(memberCreateRequest);

        MvcResult memberCreateResult = mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.success", Matchers.is(true)))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.id").exists())
                .andDo(print())
                .andReturn();

        // 4. 등록된 데이터 검증
        String memberCreateResponseBody = memberCreateResult.getResponse().getContentAsString();
        JsonNode dataNode = objectMapper.readTree(memberCreateResponseBody).get("data");
        MemberDetail responseDetail = objectMapper.treeToValue(dataNode, MemberDetail.class);

        assertThat(responseDetail.getId()).isNotNull();
        assertThat(responseDetail.getEmail()).isEqualTo(memberFixture.getEmail());
        assertThat(responseDetail.getNickname()).isNotNull();
        assertThat(responseDetail.getRole()).isEqualTo(MemberRole.USER);
        assertThat(responseDetail.getProfileImage()).isNotNull();
        assertThat(responseDetail.getRegisteredAt()).isNotNull();
    }

    // todo 중복 이메일

    // todo 유효성 검사: 올바른 이메일

    // todo 유효성 검사: 올바른 비밀번호

    // todo 비밀번호 암호화

    // 회원 등록
    private MemberDetail registerMember(String email, String password) throws Exception {
        // 1. 인증 코드 발송
        String authCode = getAuthCode(email);

        // 2. 회원 등록
        MemberCreateRequest memberCreateRequest = new MemberCreateRequest(email, password, authCode);
        String requestBody = objectMapper.writeValueAsString(memberCreateRequest);

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn();

        // 3. 등록된 데이터 검증
        String responseBody = mvcResult.getResponse().getContentAsString();
        JsonNode dataNode = objectMapper.readTree(responseBody).get("data");
        return objectMapper.treeToValue(dataNode, MemberDetail.class);
    }

    // 인증 코드 발송
    private String getAuthCode(String email) throws Exception {
        EmailCodeRequest emailCodeRequest = new EmailCodeRequest(email);
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/otp/send/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailCodeRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("data").asText();
    }
}