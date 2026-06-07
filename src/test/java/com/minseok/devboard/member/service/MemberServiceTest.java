package com.minseok.devboard.member.service;

import com.minseok.devboard.member.dto.request.LoginRequest;
import com.minseok.devboard.member.dto.request.SignupRequest;
import com.minseok.devboard.member.entity.Gender;
import com.minseok.devboard.member.entity.Member;
import com.minseok.devboard.member.entity.MemberStatus;
import com.minseok.devboard.member.entity.Role;
import com.minseok.devboard.member.exception.DuplicateEmailException;
import com.minseok.devboard.member.exception.DuplicateNicknameException;
import com.minseok.devboard.member.exception.LoginFailedException;
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
    
    //==회원가입 테스트==//
    @Test
    @DisplayName("회원가입 성공")
    void signupSuccessTest() {
        // given
        final String email = "signupSuccessTest@example.com";
        final String password = "signupSuccessTest-password";
        final String nickname = "signupSuccessTest-nickname";
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
    void signupDuplicateEmailTest() {
        // given
        final String duplicateEmail = "signupDuplicateEmailTest@example.com";
        final SignupRequest signupRequest1 = new SignupRequest(duplicateEmail,
                                                               "signupDuplicateEmailTest-password1",
                                                               "signupDupEmailTest-nickname1",
                                                               Gender.MALE);
        memberService.signup(signupRequest1);
        
        // when, then
        final SignupRequest signupRequest2 = new SignupRequest(duplicateEmail,
                                                               "signupDuplicateEmailTest-password2",
                                                               "signupDupEmailTest-nickname2",
                                                               Gender.FEMALE);
        assertThatThrownBy(() -> memberService.signup(signupRequest2))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
    }
    
    @Test
    @DisplayName("회원가입 실패 - 닉네임 중복")
    void signupDuplicateNicknameTest() {
        // given
        final String duplicateNickname = "signupDupNickname";
        final SignupRequest signupRequest1 = new SignupRequest("signupDuplicateNicknameTest1@example.com",
                                                               "signupDuplicateNicknameTest-password1",
                                                               duplicateNickname,
                                                               Gender.MALE);
        memberService.signup(signupRequest1);
        
        // when, then
        final SignupRequest signupRequest2 = new SignupRequest("signupDuplicateNicknameTest2@example.com",
                                                               "signupDuplicateNicknameTest-password2",
                                                               duplicateNickname,
                                                               Gender.MALE);
        assertThatThrownBy(() -> memberService.signup(signupRequest2))
                .isInstanceOf(DuplicateNicknameException.class)
                .hasMessage("이미 존재하는 닉네임입니다.");
    }
    
    //==로그인 테스트==//
    @Test
    @DisplayName("로그인 성공")
    void loginSuccessTest() {
        // given
        final String email = "loginSuccessTest@example.com";
        final String password = "loginSuccessTest-password";
        final String nickname = "loginSuccessTest-nickname";
        
        final Long saveId = memberService.signup(new SignupRequest(email, password, nickname, Gender.NONE));
        
        // when
        final Long loginId = memberService.login(new LoginRequest(email, password));
        
        // then
        assertThat(saveId).isEqualTo(loginId);
    }
    
    @Test
    @DisplayName("로그인 실패 - 탈퇴 회원")
    void loginDeletedMemberTest() {
        // given
        final String email = "loginDeletedMemberTest@example.com";
        final String password = "loginDeletedMemberTest-password";
        final String nickname = "loginDeletedMember-nickname";
        
        final Long savedId = memberService.signup(new SignupRequest(email, password, nickname, Gender.NONE));
        
        // 회원 탈퇴
        final Member member = memberRepository.findById(savedId)
                                              .orElseThrow();
        member.changeStatus(MemberStatus.DELETED);
        
        // when, then
        assertThatThrownBy(() -> memberService.login(new LoginRequest(email, password)))
                .isInstanceOf(LoginFailedException.class);
    }
    
    @Test
    @DisplayName("로그인 실패 - 이메일 불일치")
    void loginNotMatchEmailTest() {
        // given
        final String email = "loginNotMatchEmailTest@example.com";
        final String password = "loginNotMatchEmailTest-password";
        final String nickname = "loginNotMatchEmailTest-nick";
        
        memberService.signup(new SignupRequest(email, password, nickname, Gender.NONE));
        
        // when, then
        final String tryEmail = "loginNotMatchEmailTest-not-found@example.com";
        assertThatThrownBy(() -> memberService.login(new LoginRequest(tryEmail, password)))
                .isInstanceOf(LoginFailedException.class);
    }
    
    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void loginNotMatchPasswordTest() {
        // given
        final String email = "loginNotMatchPasswordTest@example.com";
        final String password = "loginNotMatchPasswordTest-password";
        final String nickname = "loginNotMatchPassword-nick";
        
        memberService.signup(new SignupRequest(email, password, nickname, Gender.NONE));
        
        // when, then
        final String tryPassword = "loginNotMatchPasswordTest-not-match-password";
        assertThatThrownBy(() -> memberService.login(new LoginRequest(email, tryPassword)))
                .isInstanceOf(LoginFailedException.class);
    }
}















