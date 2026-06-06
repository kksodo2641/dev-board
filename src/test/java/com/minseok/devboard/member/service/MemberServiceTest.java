package com.minseok.devboard.member.service;

import com.minseok.devboard.member.dto.request.SignupRequest;
import com.minseok.devboard.member.entity.Gender;
import com.minseok.devboard.member.entity.Member;
import com.minseok.devboard.member.entity.MemberStatus;
import com.minseok.devboard.member.entity.Role;
import com.minseok.devboard.member.exception.DuplicateEmailException;
import com.minseok.devboard.member.exception.DuplicateNicknameException;
import com.minseok.devboard.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class MemberServiceTest {
    
    @Autowired MemberRepository memberRepository;
    @Autowired MemberService memberService;
    @Autowired PasswordEncoder passwordEncoder;
    
    @Test
    @DisplayName("회원가입 성공")
    void signupSuccess() {
        // given
        final String email = "test@example.com";
        final String password = "12345678";
        final String nickname = "nickname1";
        final Gender gender = Gender.MALE;
        final SignupRequest signupRequest = new SignupRequest(email, password, nickname, gender);
        
        // when
        final Long savedId = memberService.signup(signupRequest);
        
        // then
        final Member foundMember = memberRepository.findById(savedId)
                                                   .orElseThrow();
        
        assertThat(foundMember.getEmail()).isEqualTo(email);
        assertThat(passwordEncoder.matches(password, foundMember.getPasswordHash())).isTrue();
        assertThat(foundMember.getNickname()).isEqualTo(nickname);
        assertThat(foundMember.getGender()).isEqualTo(Gender.MALE);
        assertThat(foundMember.getRole()).isEqualTo(Role.USER);
        assertThat(foundMember.getStatus()).isEqualTo(MemberStatus.ACTIVE);
    }
    
    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signupDuplicateEmail() {
        // given
        final String email = "test@example.com";
        final SignupRequest signupRequest1 = new SignupRequest(email,
                                                               "12345678",
                                                               "nickname1",
                                                               Gender.MALE);
        memberService.signup(signupRequest1);
        
        // when, then
        final SignupRequest signupRequest2 = new SignupRequest(email,
                                                               "12345678",
                                                               "nickname2",
                                                               Gender.FEMALE);
        assertThatThrownBy(() -> memberService.signup(signupRequest2))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
    }
    
    @Test
    @DisplayName("회원가입 실패 - 닉네임 중복")
    void signupDuplicateNickname() {
        // given
        final String nickname = "nickname1";
        final SignupRequest signupRequest1 = new SignupRequest("test1@example.com",
                                                               "12345678",
                                                               nickname,
                                                               Gender.MALE);
        memberService.signup(signupRequest1);
        
        // when, then
        final SignupRequest signupRequest2 = new SignupRequest("test2@example.com",
                                                               "12345678",
                                                               nickname,
                                                               Gender.MALE);
        assertThatThrownBy(() -> memberService.signup(signupRequest2))
                .isInstanceOf(DuplicateNicknameException.class)
                .hasMessage("이미 존재하는 닉네임입니다.");
    }
}















