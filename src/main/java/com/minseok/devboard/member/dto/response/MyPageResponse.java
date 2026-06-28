package com.minseok.devboard.member.dto.response;

import com.minseok.devboard.member.entity.Gender;
import com.minseok.devboard.member.entity.Member;
import com.minseok.devboard.member.entity.MemberStatus;
import com.minseok.devboard.member.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class MyPageResponse {
    
    private final String email;               // 이메일
    private final String nickname;            // 닉네임
    private final Gender gender;              // 성별
    private final Role role;                  // 권한
    private final MemberStatus status;        // 회원 상태
    private final LocalDateTime createdAt;    // 가입 일시
    
    public static MyPageResponse toResponse(final Member member) {
        assert (member != null);
        
        return new MyPageResponse(member.getEmail(),
                                  member.getNickname(),
                                  member.getGender(),
                                  member.getRole(),
                                  member.getStatus(),
                                  member.getCreatedAt());
    }
}
