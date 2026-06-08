package com.minseok.devboard.member.dto.response;

import com.minseok.devboard.member.entity.Gender;
import com.minseok.devboard.member.entity.MemberStatus;
import com.minseok.devboard.member.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyPageResponse {
    
    private String email;               // 이메일
    private String nickname;            // 닉네임
    private Gender gender;              // 성별
    private Role role;                  // 권한
    private MemberStatus status;        // 회원 상태
    private LocalDateTime createdAt;    // 가입 일시
}

















