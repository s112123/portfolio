package org.demo.backend.post.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {

    @NotBlank(message = "글 제목은 필수입니다")
    @Size(min = 1, max = 100, message = "글 제목은 최소 1자, 최대 100자까지 입력할 수 있습니다")
    private String title;

    @NotBlank(message = "글 내용은 필수입니다")
    private String content;
}
