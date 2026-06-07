package com.minseok.devboard.member.service;

import com.minseok.devboard.member.dto.request.LoginRequest;
import com.minseok.devboard.member.dto.request.SignupRequest;
import com.minseok.devboard.member.entity.Member;
import com.minseok.devboard.member.entity.MemberStatus;
import com.minseok.devboard.member.exception.DuplicateEmailException;
import com.minseok.devboard.member.exception.DuplicateNicknameException;
import com.minseok.devboard.member.exception.LoginFailedException;
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
     * - 이메일 중복 허용 X
     * - 닉네임 중복 허용 X
     * - 탈퇴 회원 이메일/닉네임 재사용 허용 X
     * - 비밀번호 -> BCrypt 해시값 저장
     */
    @Transactional
    public Long signup(final SignupRequest request) {
        assert (request != null);
        
        validateDuplicateEmail(request.getEmail());
        validateDuplicateNickname(request.getNickname());
        
        final Member savedMember = memberRepository.save(Member.createMember(request.getEmail(),
                                                                             passwordEncoder.encode(request.getPassword()),
                                                                             request.getNickname(),
                                                                             request.getGender()));
        return savedMember.getId();
    }
    
    public Long login(final LoginRequest request) {
        assert (request != null);
        
        final String loginFailMessage = "이메일 또는 비밀번호가 일치하지 않습니다.";
        
        final Member foundMember = memberRepository.findByEmail(request.getEmail())
                                                   .orElseThrow(() -> new LoginFailedException(loginFailMessage));
        
        if (foundMember.getStatus() != MemberStatus.ACTIVE
                || !passwordEncoder.matches(request.getPassword(),
                                            foundMember.getPasswordHash())) {
            throw new LoginFailedException(loginFailMessage);
        }
        
        return foundMember.getId();
    }
    
    private void validateDuplicateEmail(final String email) {
        assert (email != null);
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다.");
        }
    }
    
    private void validateDuplicateNickname(final String nickname) {
        assert (nickname != null);
        if (memberRepository.existsByNickname(nickname)) {
            throw new DuplicateNicknameException("이미 존재하는 닉네임입니다.");
        }
    }
}

















