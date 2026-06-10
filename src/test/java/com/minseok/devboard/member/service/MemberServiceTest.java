package com.minseok.devboard.member.service;

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
    
    //==회원가입==//
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
    
    //==로그인==//
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
    void loginWithdrawMemberTest() {
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
    
    //==회원정보 수정==//
    @Test
    @DisplayName("회원정보 수정 성공")
    void updateProfileTest() {
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
    @DisplayName("회원정보 수정 성공 - 닉네임 유지, 성별만 수정")
    void updateProfileWithSameNicknameTest() {
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
    @DisplayName("회원정보 수정 실패 - 중복 닉네임으로 수정")
    void updateProfileDuplicateNicknameTest() {
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
    @DisplayName("회원정보 수정 실패 - 탈퇴 회원")
    void updateProfileWithdrawMemberTest() {
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
    void withdrawTest() {
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















