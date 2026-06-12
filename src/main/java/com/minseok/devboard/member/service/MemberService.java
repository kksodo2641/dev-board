package com.minseok.devboard.member.service;

import com.minseok.devboard.member.dto.request.LoginRequest;
import com.minseok.devboard.member.dto.request.SignupRequest;
import com.minseok.devboard.member.dto.request.UpdateMemberRequest;
import com.minseok.devboard.member.dto.response.MyPageResponse;
import com.minseok.devboard.member.entity.Member;
import com.minseok.devboard.member.entity.MemberStatus;
import com.minseok.devboard.member.exception.DuplicateEmailException;
import com.minseok.devboard.member.exception.DuplicateNicknameException;
import com.minseok.devboard.member.exception.LoginFailedException;
import com.minseok.devboard.member.exception.MemberNotFoundException;
import com.minseok.devboard.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * [회원가입]
     * - 이메일/닉네임 중복 허용 X
     * - 탈퇴 회원 이메일/닉네임 재사용 허용 X
     * - 비밀번호 -> BCrypt 해시값 저장
     */
    @Transactional
    public Long signup(final SignupRequest request) {
        assert (request != null);
        
        validateDuplicateEmail(request.getEmail());
        validateDuplicateNickname(request.getNickname());
        
        final Member savedMember = memberRepository.save(Member.create(request.getEmail(),
                                                                       passwordEncoder.encode(request.getPassword()),
                                                                       request.getNickname(),
                                                                       request.getGender()));
        return savedMember.getId();
    }
    
    public Long login(final LoginRequest request) {
        assert (request != null);
        
        final Member foundMember = memberRepository.findByEmail(request.getEmail())
                                                   .orElseThrow(LoginFailedException::new);
        
        if (foundMember.getStatus() != MemberStatus.ACTIVE
                || !passwordEncoder.matches(request.getPassword(),
                                            foundMember.getPasswordHash())) {
            throw new LoginFailedException();
        }
        
        return foundMember.getId();
    }
    
    public MyPageResponse getMyPage(final Long memberId) {
        assert (memberId != null);
        
        return memberRepository.findById(memberId)
                               .map(m -> new MyPageResponse(m.getEmail(),
                                                            m.getNickname(),
                                                            m.getGender(),
                                                            m.getRole(),
                                                            m.getStatus(),
                                                            m.getCreatedAt()))
                               .orElseThrow(MemberNotFoundException::new);
    }
    
    @Transactional
    public void withdraw(final Long memberId) {
        assert (memberId != null);
        
        final Member member = memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                                              .orElseThrow(MemberNotFoundException::new);
        member.withdraw();
    }
    
    @Transactional
    public void updateProfile(final Long memberId,
                              final UpdateMemberRequest request) {
        assert (memberId != null);
        assert (request != null);
        
        final Member member = memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                                              .orElseThrow(MemberNotFoundException::new);
        
        final String newNickname = request.getNickname();
        
        if (!member.getNickname().equals(newNickname)) {
            validateDuplicateNickname(newNickname);
        }
        
        member.updateProfile(newNickname, request.getGender());
    }
    
    private void validateDuplicateEmail(final String email) {
        assert (email != null);
        
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }
    }
    
    private void validateDuplicateNickname(final String nickname) {
        assert (nickname != null);
        
        if (memberRepository.existsByNickname(nickname)) {
            throw new DuplicateNicknameException();
        }
    }
}

















