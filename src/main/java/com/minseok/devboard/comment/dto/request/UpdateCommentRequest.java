package com.minseok.devboard.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommentRequest {
    
    @NotBlank(message = "내용을 입력해주세요.")
    @Size(max = 2_000, message = "최대 2,000자까지만 작성할 수 있습니다.")
    private String content;
}
