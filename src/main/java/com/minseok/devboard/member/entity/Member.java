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
    @Column(name = "member_id", nullable = false)
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
    
    //==생성 메서드==//
    
    public static Member create(final String email,
                                final String passwordHash,
                                final String nickname,
                                final Gender gender) {
        final Member member = new Member();
        member.changeEmail(email);
        member.changePasswordHash(passwordHash);
        member.updateProfile(nickname, gender);
        
        // 생성 시 기본값
        member.changeRole(Role.USER); // 회원 생성 시 초기 권한은 USER
        member.status = MemberStatus.ACTIVE; // 회원 생성 시 초기 상태는 ACTIVE
        
        return member;
    }
    
    //==비즈니스 로직==//
    
    /**
     * 회원정보(닉네임, 성별) 수정
     */
    public void updateProfile(final String nickname,
                              final Gender gender) {
        requireNonNull(gender);
        validateNotBlankText("nickname", nickname);
        
        this.nickname = nickname;
        this.gender = gender;
    }
    
    /**
     * 회원 탈퇴
     */
    public void withdraw() {
        if (status == MemberStatus.DELETED) {
            throw new IllegalStateException("이미 탈퇴한 회원입니다.");
        }
        
        status = MemberStatus.DELETED;
    }
    
    //==내부 상태 변경==//
    
    private void changeEmail(final String email) {
        validateNotBlankText("email", email);
        this.email = email;
    }
    
    private void changePasswordHash(final String passwordHash) {
        validateNotBlankText("passwordHash", passwordHash);
        this.passwordHash = passwordHash;
    }
    
    /**
     * 관리자 권한 관리 정책 확정 시, 리팩토링 예정
     */
    private void changeRole(final Role role) {
        requireNonNull(role);
        this.role = role;
    }
    
    private static void validateNotBlankText(final String fieldName, final String value) {
        assert (fieldName != null);
        
        requireNonNull(value);
        
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "은 공백일 수 없습니다.");
        }
    }
}

















