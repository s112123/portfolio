package org.demo.backend.otp.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailCodeCreateRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email
    private String email;
}
