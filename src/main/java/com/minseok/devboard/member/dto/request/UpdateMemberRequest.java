package com.minseok.devboard.member.dto.request;

import com.minseok.devboard.member.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberRequest {
    
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 30, message = "닉네임은 최소 2글자, 최대 30자까지만 가능합니다.")
    private String nickname;
    
    @NotNull(message = "성별은 필수입니다.")
    private Gender gender;
}
