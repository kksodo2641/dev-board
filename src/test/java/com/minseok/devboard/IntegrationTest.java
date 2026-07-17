package com.minseok.devboard;

import com.minseok.devboard.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTest {
    
    protected static final String ADMIN_EMAIL = "testAdmin@devboard.com";
    
    @Autowired
    private MemberRepository memberRepository;
    
    protected Long getAdminMemberId() {
        return memberRepository.findByEmail(ADMIN_EMAIL)
                               .orElseThrow()
                               .getId();
    }
}
