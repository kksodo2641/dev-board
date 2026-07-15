package com.minseok.devboard.member.service;

import com.minseok.devboard.IntegrationTest;
import com.minseok.devboard.member.dto.request.LoginRequest;
import com.minseok.devboard.member.dto.request.SignupRequest;
import com.minseok.devboard.member.dto.request.UpdateMemberRequest;
import com.minseok.devboard.member.entity.Gender;
import com.minseok.devboard.member.entity.Member;
import com.minseok.devboard.member.entity.MemberStatus;
import com.minseok.devboard.member.entity.Role;
import com.minseok.devboard.member.exception.DuplicateEmailException;
import com.minseok.devboard.member.exception.DuplicateNicknameException;
import com.minseok.devboard.member.exception.LoginFailedException;
import com.minseok.devboard.member.exception.MemberNotFoundException;
import com.minseok.devboard.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberServiceTest extends IntegrationTest {
    
    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired PasswordEncoder passwordEncoder;
    
    //==회원가입==//
    
    @Test
    @DisplayName("중복되지 않는 이메일/비밀번호를 사용해 회원가입할 수 있다.")
    void signup() {
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
    @DisplayName("이미 존재하는 이메일로 회원가입할 수 없다.")
    void signupWithDuplicateEmail() {
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
    @DisplayName("이미 존재하는 닉네임으로 회원가입할 수 없다.")
    void signupWithDuplicateNickname() {
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
    
    //==로그인==//
    
    @Test
    @DisplayName("이메일과 비밀번호가 일치하면, 로그인에 성공한다.")
    void login() {
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
    @DisplayName("탈퇴 회원은 로그인할 수 없다.")
    void loginByWithdrawnMember() {
        // given
        final String email = "loginWithdrawMemberTest@example.com";
        final String password = "loginWithdrawMemberTest-password";
        final String nickname = "loginWithdrawMember-nickname";
        
        final Long savedId = memberService.signup(new SignupRequest(email, password, nickname, Gender.NONE));
        
        // 회원 탈퇴
        final Member member = memberRepository.findById(savedId)
                                              .orElseThrow();
        member.withdraw();
        
        // when, then
        assertThatThrownBy(() -> memberService.login(new LoginRequest(email, password)))
                .isInstanceOf(LoginFailedException.class);
    }
    
    @Test
    @DisplayName("이메일이 일치하지 않으면, 로그인에 실패한다.")
    void loginWithMismatchEmail() {
        // given
        final String email = "loginMismatchEmailTest@example.com";
        final String password = "loginMismatchEmailTest-password";
        final String nickname = "loginMismatchEmailTest-nick";
        
        memberService.signup(new SignupRequest(email, password, nickname, Gender.NONE));
        
        // when, then
        final String tryEmail = "loginMismatchEmailTest-not-found@example.com";
        assertThatThrownBy(() -> memberService.login(new LoginRequest(tryEmail, password)))
                .isInstanceOf(LoginFailedException.class);
    }
    
    @Test
    @DisplayName("비밀번호가 일치하지 않으면, 로그인에 실패한다.")
    void loginWithMismatchPassword() {
        // given
        final String email = "loginMismatchPasswordTest@example.com";
        final String password = "loginMismatchPasswordTest-password";
        final String nickname = "loginMismatchPassword-nick";
        
        memberService.signup(new SignupRequest(email, password, nickname, Gender.NONE));
        
        // when, then
        final String tryPassword = "loginMismatchPasswordTest-not-match-password";
        assertThatThrownBy(() -> memberService.login(new LoginRequest(email, tryPassword)))
                .isInstanceOf(LoginFailedException.class);
    }
    
    //==회원정보 수정==//
    
    @Test
    @DisplayName("회원정보를 수정할 수 있다.")
    void updateProfile() {
        // given
        final String email = "updateProfileTest@test.com";
        final String password = "updateProfileTest-password";
        final String nickname = "updateProfile-before";
        final Gender gender = Gender.MALE;
        
        final Long memberId = memberService.signup(new SignupRequest(email, password, nickname, gender));
        
        // when
        final String newNickname = "updateProfile-after";
        final Gender newGender = Gender.NONE;
        
        memberService.updateProfile(memberId, new UpdateMemberRequest(newNickname,
                                                                      newGender));
        
        // then
        final Member foundMember = memberRepository.findById(memberId)
                                                   .orElseThrow();
        
        assertThat(foundMember.getNickname()).isEqualTo(newNickname);
        assertThat(foundMember.getGender()).isEqualTo(newGender);
    }
    
    @Test
    @DisplayName("회원정보 수정 시, 기존 닉네임을 유지할 수 있다.")
    void updateProfileWithSameNickname() {
        // given
        final String email = "updateProfileWithSameNickname@test.com";
        final String password = "updateProfileWithSameNickname-password";
        final String nickname = "updateProfile-SameNickname";
        final Gender gender = Gender.FEMALE;
        
        final Long memberId = memberService.signup(new SignupRequest(email, password, nickname, gender));
        
        // when
        final Gender newGender = Gender.NONE;
        
        memberService.updateProfile(memberId, new UpdateMemberRequest(nickname,
                                                                      newGender));
        
        // then
        final Member foundMember = memberRepository.findById(memberId)
                                                   .orElseThrow();
        
        assertThat(foundMember.getNickname()).isEqualTo(nickname);
        assertThat(foundMember.getGender()).isEqualTo(newGender);
    }
    
    @Test
    @DisplayName("이미 존재하는 닉네임으로 회원정보를 수정할 수 없다.")
    void updateProfileWithDuplicateNickname() {
        // given
        final String nickname1 = "updateProfileDupNickname-1";
        final String nickname2 = "updateProfileDupNickname-2";
        
        memberService.signup(new SignupRequest("updateProfileDupNicknameTest-1@test.com",
                                               "password",
                                               nickname1,
                                               Gender.NONE));
        
        final Long targetId = memberService.signup(new SignupRequest("updateProfileDupNicknameTest-2@test.com",
                                                                     "password",
                                                                     nickname2,
                                                                     Gender.MALE));
        
        // when, then
        final UpdateMemberRequest request = new UpdateMemberRequest(nickname1, Gender.FEMALE);
        
        assertThatThrownBy(() -> memberService.updateProfile(targetId, request))
                .isInstanceOf(DuplicateNicknameException.class);
        
        final Member foundMember = memberRepository.findById(targetId)
                                                   .orElseThrow();
        
        assertThat(foundMember.getNickname()).isEqualTo(nickname2);
        assertThat(foundMember.getGender()).isEqualTo(Gender.MALE);
    }
    
    @Test
    @DisplayName("탈퇴 회원은 회원정보를 수정할 수 없다.")
    void updateProfileByWithdrawnMember() {
        // given
        final String email = "updateProfileWithdrawMemberTest@test.com";
        final String password = "updateProfileWithdrawMemberTest-password";
        final String nickname = "updateProfileWithdraw-before";
        final Gender gender = Gender.MALE;
        
        final Long memberId = memberService.signup(new SignupRequest(email, password, nickname, gender));
        
        // 회원 탈퇴
        memberService.withdraw(memberId);
        
        // when, then
        final UpdateMemberRequest request = new UpdateMemberRequest("updateProfileWithdraw-after",
                                                                    Gender.FEMALE);
        
        assertThatThrownBy(() -> memberService.updateProfile(memberId, request))
                .isInstanceOf(MemberNotFoundException.class);
    }
    
    //==회원 탈퇴==//
    
    @Test
    @DisplayName("회원 탈퇴 시 DELETED 상태가 된다.")
    void withdraw() {
        // given
        final String email = "withdrawTest@test.com";
        final String password = "withdrawTest-password";
        final String nickname = "withdrawTest-nickname";
        
        final Long id = memberService.signup(new SignupRequest(email, password, nickname, Gender.FEMALE));
        
        // when
        memberService.withdraw(id);
        
        // then
        final Member foundMember = memberRepository.findById(id)
                                                   .orElseThrow();
        assertThat(foundMember.getStatus()).isEqualTo(MemberStatus.DELETED);
    }
}
