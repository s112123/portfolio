package org.demo.backend.member.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberCreateRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min=4, max=8, message = "비밀번호는 4~8자리입니다")
    private String password;

    @NotBlank(message = "인증 코드는 필수입니다")
    @Size(min=6, max=6, message = "인증 코드는 6자리입니다")
    private String code;
}
