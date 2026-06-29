package com.minseok.devboard.member.service;

import com.minseok.devboard.member.dto.request.LoginRequest;
import com.minseok.devboard.member.dto.request.SignupRequest;
import com.minseok.devboard.member.dto.request.UpdateMemberRequest;
import com.minseok.devboard.member.dto.response.MyPageResponse;
import com.minseok.devboard.member.entity.Member;
import com.minseok.devboard.member.exception.DuplicateEmailException;
import com.minseok.devboard.member.exception.DuplicateNicknameException;
import com.minseok.devboard.member.exception.LoginFailedException;
import com.minseok.devboard.member.exception.MemberNotFoundException;
import com.minseok.devboard.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.minseok.devboard.member.entity.MemberStatus.ACTIVE;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 회원가입
     * <p>
     * 비밀번호: BCrypt 해시로 저장
     *
     * @return 회원 ID
     * @throws DuplicateEmailException    이미 사용 중인 이메일인 경우
     * @throws DuplicateNicknameException 이미 사용 중인 닉네임인 경우
     */
    @Transactional
    public Long signup(final SignupRequest request) {
        assert (request != null);
        
        validateDuplicateEmail(request.getEmail());
        validateDuplicateNickname(request.getNickname());
        
        final Member member = Member.create(request.getEmail(),
                                            passwordEncoder.encode(request.getPassword()),
                                            request.getNickname(),
                                            request.getGender());
        
        return memberRepository.save(member)
                               .getId();
    }
    
    /**
     * 로그인
     *
     * @throws LoginFailedException 이메일 또는 비밀번호가 일치하지 않거나
     *                              탈퇴한 회원인 경우
     */
    public Long login(final LoginRequest request) {
        assert (request != null);
        
        final Member foundMember = memberRepository.findByEmail(request.getEmail())
                                                   .orElseThrow(LoginFailedException::new);
        
        if (foundMember.getStatus() != ACTIVE
                || !passwordEncoder.matches(request.getPassword(),
                                            foundMember.getPasswordHash())) {
            throw new LoginFailedException();
        }
        
        return foundMember.getId();
    }
    
    /**
     * 마이페이지 조회
     *
     * @throws MemberNotFoundException 활성 회원을 찾을 수 없는 경우
     */
    public MyPageResponse getMyPage(final Long memberId) {
        assert (memberId != null);
        
        final Member member = findActiveMemberElseThrow(memberId);
        return MyPageResponse.toResponse(member);
    }
    
    /**
     * 회원 탈퇴
     *
     * @throws MemberNotFoundException 활성 회원을 찾을 수 없는 경우
     */
    @Transactional
    public void withdraw(final Long memberId) {
        assert (memberId != null);
        
        final Member member = findActiveMemberElseThrow(memberId);
        member.withdraw();
    }
    
    /**
     * 회원정보 수정
     *
     * @throws MemberNotFoundException    활성 회원을 찾을 수 없는 경우
     * @throws DuplicateNicknameException 이미 사용 중인 닉네임인 경우
     */
    @Transactional
    public void updateProfile(final Long memberId,
                              final UpdateMemberRequest request) {
        assert (memberId != null);
        assert (request != null);
        
        final Member member = findActiveMemberElseThrow(memberId);
        
        final String newNickname = request.getNickname();
        
        if (!member.getNickname().equals(newNickname)) {
            validateDuplicateNickname(newNickname);
        }
        
        member.updateProfile(newNickname, request.getGender());
    }
    
    //==조회 메서드==//
    
    /**
     * 관리자 여부 확인
     *
     * @throws MemberNotFoundException 활성 회원을 찾을 수 없는 경우
     */
    public boolean isAdmin(final Long memberId) {
        assert (memberId != null);
        return findActiveMemberElseThrow(memberId).isAdmin();
    }
    
    //==내부 사용 메서드==//
    
    /**
     * 활성(ACTIVE) 회원 조회
     *
     * @throws MemberNotFoundException 활성 회원을 찾을 수 없는 경우
     */
    private Member findActiveMemberElseThrow(final Long memberId) {
        assert (memberId != null);
        return memberRepository.findByIdAndStatus(memberId, ACTIVE)
                               .orElseThrow(MemberNotFoundException::new);
    }
    
    //==검증 메서드==//
    
    /**
     * 중복 이메일 검증
     *
     * @throws DuplicateEmailException 이미 사용 중인 이메일인 경우
     */
    private void validateDuplicateEmail(final String email) {
        assert (email != null);
        
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }
    }
    
    /**
     * 중복 닉네임 검증
     *
     * @throws DuplicateNicknameException 이미 사용 중인 닉네임인 경우
     */
    private void validateDuplicateNickname(final String nickname) {
        assert (nickname != null);
        
        if (memberRepository.existsByNickname(nickname)) {
            throw new DuplicateNicknameException();
        }
    }
}
