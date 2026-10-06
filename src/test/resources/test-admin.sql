-- 관리자 이메일이 없을 때만 새 관리자 회원 추가
INSERT INTO member (
    email,
    password_hash,
    nickname,
    gender,
    role,
    status,
    created_at,
    updated_at
)
SELECT
    'testAdmin@devboard.com',
    'test-only-unused-password-hash',
    'test-admin-fixture',
    'NONE',
    'ADMIN',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM member
    WHERE email = 'testAdmin@devboard.com'
);

-- 기존 관리자 회원의 테스트 선행 조건 보장
UPDATE member
SET role = 'ADMIN',
    status = 'ACTIVE'
WHERE email = 'testAdmin@devboard.com';
