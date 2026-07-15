package com.minseok.devboard;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTest {
    
    protected static final String ADMIN_EMAIL = "testAdmin@devboard.com";
}
