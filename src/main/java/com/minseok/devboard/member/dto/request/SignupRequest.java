package com.minseok.devboard.member.dto.request;

import com.minseok.devboard.member.entity.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignupRequest {
    
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email
    private String email;
    
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, message = "최소 8자 이상이어야 합니다.")
    private String password;
    
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 30, message = "닉네임은 최소 2글자, 최대 30자까지만 가능합니다.")
    private String nickname;
    
    @NotNull(message = "성별은 필수입니다.")
    private Gender gender;
    
    public SignupRequest(final String email,
                         final String password,
                         final String nickname,
                         final Gender gender) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.gender = gender;
    }
}
