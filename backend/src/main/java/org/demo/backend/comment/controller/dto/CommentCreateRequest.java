package org.demo.backend.comment.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {

    @Min(value = 1, message = "상위 댓글 ID는 1이상이어야 합니다")
    private Long parentId;

    @NotNull(message = "게시글 ID는 필수입니다")
    @Min(value = 1, message = "게시글 ID는 1이상이어야 합니다")
    private Long postId;

    @NotNull(message = "작성자 ID는 필수입니다")
    @Min(value = 1, message = "작성자 ID는 1이상이어야 합니다")
    private Long writerId;

    @NotBlank(message = "댓글 내용은 필수이며 공백일 수 없습니다")
    @Size(min = 1, max = 100, message = "댓글은 100자까지 입력 가능합니다")
    private String content;
}
