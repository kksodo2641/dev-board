package com.minseok.devboard.member.entity;

import com.minseok.devboard.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PROTECTED;

@Entity @Table(name = "member")
@NoArgsConstructor(access = PROTECTED)
@Getter
public class Member extends BaseTimeEntity {
    
    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "member_id")
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email; // 로그인 ID로 사용
    
    @Column(nullable = false)
    private String passwordHash; // BCrypt Hash
    
    @Column(nullable = false, unique = true, length = 30)
    private String nickname;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;
    
    public static Member createMember(final String email,
                                      final String passwordHash,
                                      final String nickname,
                                      final Gender gender) {
        final Member member = new Member();
        member.changeEmail(email);
        member.changePasswordHash(passwordHash);
        member.changeNickname(nickname);
        member.changeGender(gender);
        member.changeRole(Role.USER);
        member.changeStatus(MemberStatus.ACTIVE);
        return member;
    }
    
    public void changeEmail(final String email) {
        validateText(email, "email");
        this.email = email;
    }
    
    public void changePasswordHash(final String passwordHash) {
        validateText(passwordHash, "passwordHash");
        this.passwordHash = passwordHash;
    }
    
    public void changeNickname(final String nickname) {
        validateText(nickname, "nickname");
        this.nickname = nickname;
    }
    
    public void changeGender(final Gender gender) {
        requireNonNull(gender);
        this.gender = gender;
    }
    
    /**
     * 관리자 권한 관리 정책 확정 시, 리팩토링 예정
     */
    public void changeRole(final Role role) {
        requireNonNull(role);
        this.role = role;
    }
    
    /**
     * 회원 탈퇴 정책 확정 시, 리팩토링 예정
     */
    public void changeStatus(final MemberStatus status) {
        requireNonNull(status);
        this.status = status;
    }
    
    private static void validateText(final String value,
                                     final String fieldName) {
        requireNonNull(value);
        
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "은 공백일 수 없습니다.");
        }
    }
}

















