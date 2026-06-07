package com.minseok.devboard.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email
    private String email;
    
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}

















