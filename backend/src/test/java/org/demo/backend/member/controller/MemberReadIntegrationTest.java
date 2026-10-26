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
import org.demo.backend.otp.controller.dto.EmailCodeCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@IntegrationTest
public class MemberReadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OtpService otpService;

//    @Test
//    @DisplayName("회원 조회: 성공")
//    void readById_success() throws Exception {
//        // 1. 사용자 정보
//        MemberFixture memberFixture = MemberFixture.create();
//        String email = memberFixture.getEmail();
//        String password = memberFixture.getPassword();
//
//        // 2. 회원 등록
//        MemberDetail savedMemberDetail = registerMember(email, password);
//        Long savedMemberId = savedMemberDetail.getId();
//
//        // 3. 회원 조회
//        MvcResult mvcResult = mockMvc.perform(get("/api/v1/members/" + savedMemberId))
//                .andExpect(status().isOk())
//                .andReturn();
//        String responseBody = mvcResult.getResponse().getContentAsString();
//        JsonNode dataNode = objectMapper.readTree(responseBody).get("data");
//        MemberDetail memberDetail = objectMapper.treeToValue(dataNode, MemberDetail.class);
//
//        // 4. 조회된 데이터 검증
//        assertThat(memberDetail.getEmail()).isEqualTo(email);
//        assertThat(memberDetail.getRole()).isEqualTo(MemberRole.USER);
//    }

//    // 회원 등록
//    private MemberDetail registerMember(String email, String password) throws Exception {
//        // 1. 인증 코드 발송
//        String authCode = getAuthCode(email);
//
//        // 2. 회원 등록
//        MemberCreateRequest memberCreateRequest = new MemberCreateRequest(email, password, authCode);
//        String requestBody = objectMapper.writeValueAsString(memberCreateRequest);
//
//        MvcResult mvcResult = mockMvc.perform(post("/api/v1/members")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isCreated())
//                .andReturn();
//
//        // 3. 등록된 데이터 검증
//        String responseBody = mvcResult.getResponse().getContentAsString();
//        JsonNode dataNode = objectMapper.readTree(responseBody).get("data");
//        return objectMapper.treeToValue(dataNode, MemberDetail.class);
//    }
//
//    // 인증 코드 발송
//    private String getAuthCode(String email) throws Exception {
//        EmailCodeCreateRequest emailCodeCreateRequest = new EmailCodeCreateRequest(email);
//        MvcResult mvcResult = mockMvc.perform(post("/api/v1/otp/send/email")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(emailCodeCreateRequest)))
//                .andExpect(status().isOk())
//                .andDo(print())
//                .andReturn();
//        String responseBody = mvcResult.getResponse().getContentAsString();
//        return objectMapper.readTree(responseBody).get("data").asText();
//    }
}
