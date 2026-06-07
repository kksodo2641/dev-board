# Project Progress

## Current Branch

feature/member

---

## Completed

### Common

* Spring Boot 프로젝트 초기 설정
* Git / GitHub 연동
* BaseTimeEntity 구현
* JPA Auditing 적용
* schema.sql 작성 및 관리

### Documentation

* README 작성
* Domain Model 문서 작성
* Database Design 문서 작성

### Member

#### Domain

* Member Entity 구현
* Gender Enum 구현
* Role Enum 구현
* MemberStatus Enum 구현
* Member Repository 구현

#### Signup

* SignupRequest DTO 구현
* BCrypt PasswordEncoder 적용
* 회원가입 서비스 구현
* 회원가입 Controller 구현
* 회원가입 View 구현
* 이메일 중복 검증 구현
* 닉네임 중복 검증 구현
* 회원가입 테스트 작성

#### Home

* HomeController 구현
* Home View 구현
* Home CSS 적용

---

## In Progress

### Member

* Login Feature

---

## Next Tasks

### Login

* [ ] LoginRequest DTO
* [ ] MemberRepository.findByEmail()
* [ ] LoginFailedException
* [ ] MemberService.login()
* [ ] Login Service Test
* [ ] LoginController
* [ ] login.html
* [ ] HttpSession 적용
* [ ] Logout 기능 구현

### Member

* [ ] MyPage 기능 설계
* [ ] 회원 탈퇴 기능 구현

---

## Member Business Rules

### Signup

* 이메일(email)을 로그인 ID로 사용한다.
* 이메일은 중복될 수 없다.
* 닉네임은 중복될 수 없다.
* 비밀번호는 최소 8자 이상이어야 한다.
* 비밀번호는 BCrypt 해시 형태로 저장한다.
* 회원가입 시 USER 권한을 부여한다.
* 회원가입 시 ACTIVE 상태로 생성한다.

### Login / Logout

* 이메일과 비밀번호로 로그인한다.
* 로그인 실패 시 동일한 오류 메시지를 사용한다.
* ACTIVE 상태의 회원만 로그인할 수 있다.
* 로그인 상태는 HttpSession으로 관리한다.
* 로그아웃 시 세션을 무효화한다.

### Withdrawal

* 회원은 스스로 탈퇴할 수 있다.
* 탈퇴 시 상태는 DELETED가 된다.
* 소프트 삭제를 적용한다.
* 탈퇴 회원의 게시글 및 댓글은 유지한다.
* 탈퇴 회원의 이메일과 닉네임은 재사용할 수 없다.

---

## Latest Commit

a416809 feat: implement member signup feature
