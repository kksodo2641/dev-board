# Project Progress

## Current Branch

feature/member

---

## Completed

### Common

- Spring Boot 프로젝트 초기 설정
- Git / GitHub 연동
- BaseTimeEntity 구현
- JPA Auditing 적용
- schema.sql 작성 및 관리

### Documentation

- README 작성
- Domain Model 문서 작성
- Database Design 문서 작성

### Member

#### Domain

- Member Entity 구현
- Gender Enum 구현
- Role Enum 구현
- MemberStatus Enum 구현
- Member Repository 구현

#### Signup

- SignupRequest DTO 구현
- BCrypt PasswordEncoder 적용
- 회원가입 서비스 구현
- 회원가입 Controller 구현
- 회원가입 View 구현
- 이메일 중복 검증 구현
- 닉네임 중복 검증 구현
- 회원가입 테스트 작성

#### Login / Logout
- LoginRequest DTO 구현
- MemberRepository.findByEmail() 구현
- LoginFailedException 구현
- MemberService.login() 구현
- 로그인 테스트 작성
- 로그인 Controller 구현
- 로그인 View 구현
- HttpSession 기반 로그인 구현
- 로그아웃 기능 구현

#### MyPage
- MyPageResponse DTO 구현
- 마이페이지 조회 서비스 구현
- 마이페이지 Controller 구현
- 마이페이지 View 구현
- 로그인 후 원래 요청 페이지 복귀(redirectURL) 구현
- Enum 사용자 친화적 표시 적용
  - ACTIVE → 활성
  - DELETED → 탈퇴
  - USER → 일반 회원
  - ADMIN → 관리자
  - MALE → 남성
  - FEMALE → 여성
  - NONE → 선택안함

#### Withdrawal
- 회원 탈퇴 기능 구현
- Soft Delete 정책 적용
- 회원 탈퇴 테스트 작성
- 회원 탈퇴 View 연동
- 탈퇴 시 세션 무효화 처리

#### Authentication
- LoginCheckInterceptor 구현
- WebConfig 구현
- 인터셉터 기반 인증 처리 적용
- Controller의 수동 로그인 체크 제거
- th:action 기반 로그인 흐름으로 리팩토링

#### Home

- HomeController 구현
- Home View 구현
- Home CSS 적용
- 로그인 상태에 따른 화면 분기 구현

---

## In Progress

### Member

- 회원정보 수정 기능 설계

---

## Next Tasks

### Member

- [ ] 회원정보 수정 기능 구현
  - 닉네임 수정
  - 성별 수정
  - 닉네임 중복 검증
  - 수정 화면 구현

### Board
- [ ] 게시판 기능 설계
- [ ] 게시판 등록 기능
- [ ] 게시판 목록 조회 기능
- [ ] 게시판 상세 조회 기능

---

## Latest Commit

[eaa7f25] feat: add member withdrawal and interceptor-based authentication
