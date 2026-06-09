package com.minseok.devboard.member.repository;

import com.minseok.devboard.member.entity.Member;
import com.minseok.devboard.member.entity.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    
    boolean existsByEmail(String email);
    
    boolean existsByNickname(String nickname);
    
    Optional<Member> findByEmail(String email);
    
    Optional<Member> findByIdAndStatus(Long id, MemberStatus status);
    
    boolean existsByIdAndStatus(Long id, MemberStatus memberStatus);
}

















