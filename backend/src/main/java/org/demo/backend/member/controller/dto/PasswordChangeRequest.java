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
public class PasswordChangeRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email
    private String email;
}
