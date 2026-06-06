package com.minseok.devboard.member.service;

import com.minseok.devboard.member.dto.request.SignupRequest;
import com.minseok.devboard.member.entity.Member;
import com.minseok.devboard.member.exception.DuplicateEmailException;
import com.minseok.devboard.member.exception.DuplicateNicknameException;
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
    
    @Transactional
    public Long signup(final SignupRequest request) {
        validateDuplicateEmail(request.getEmail());
        validateDuplicateNickname(request.getNickname());
        
        final Member savedMember = memberRepository.save(Member.createMember(request.getEmail(),
                                                                             passwordEncoder.encode(request.getPassword()),
                                                                             request.getNickname(),
                                                                             request.getGender()));
        return savedMember.getId();
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

















