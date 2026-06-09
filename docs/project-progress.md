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
- 비로그인 접근 시 로그인 페이지 리다이렉트 구현
- 로그인 후 원래 요청 페이지 복귀(redirectURL) 구현

#### Home

- HomeController 구현
- Home View 구현
- Home CSS 적용
- 로그인 상태에 따른 화면 분기 구현

---

## In Progress

### Member

- 회원 탈퇴 기능 설계 및 구현

---

## Next Tasks

### Member

- [ ] 회원 탈퇴 기능 구현
- [ ] 회원 상태 사용자 친화적 표시
  - ACTIVE → 활성
  - DELETED → 탈퇴
  - USER → 일반 회원
- [ ] LoginCheckInterceptor 적용

### Board
- [ ] 게시판 기능 설계
- [ ] 게시판 등록 기능
- [ ] 게시판 목록 조회 기능
- [ ] 게시판 상세 조회 기능

---

## Member Business Rules

### Signup

- 이메일(email)을 로그인 ID로 사용한다.
- 이메일은 중복될 수 없다.
- 닉네임은 중복될 수 없다.
- 비밀번호는 최소 8자 이상이어야 한다.
- 비밀번호는 BCrypt 해시 형태로 저장한다.
- 회원가입 시 USER 권한을 부여한다.
- 회원가입 시 ACTIVE 상태로 생성한다.

### Login / Logout

- 이메일과 비밀번호로 로그인한다.
- 로그인 성공 시 memberId만 세션에 저장한다.
- Member 객체 전체는 세션에 저장하지 않는다.
- 로그인 실패 시 동일한 오류 메시지를 사용한다.
- ACTIVE 상태의 회원만 로그인할 수 있다.
- 로그인 상태는 HttpSession으로 관리한다.
- 로그아웃 시 세션을 무효화한다.

### MyPage
- 마이페이지 URL은 GET /members/me 를 사용한다.
- URL에 memberId를 노출하지 않는다.
- 비로그인 사용자는 로그인 페이지로 이동한다.
- 로그인 성공 시 원래 요청한 페이지로 복귀할 수 있다.

### Withdrawal

- 회원은 스스로 탈퇴할 수 있다.
- 탈퇴 시 상태는 DELETED가 된다.
- 소프트 삭제를 적용한다.
- 탈퇴 회원의 게시글 및 댓글은 유지한다.
- 탈퇴 회원의 이메일과 닉네임은 재사용할 수 없다.

---

## Latest Commit

391f587 feat: add member mypage and login redirect support
