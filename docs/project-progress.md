# Project Progress

## Current Branch

feature/board

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
- Project Progress 문서 작성 및 관리

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

#### Profile Update

- UpdateMemberRequest DTO 구현
- 회원정보 수정 Service 구현
- 회원정보 수정 Controller 구현
- 회원정보 수정 View 구현
- 닉네임 수정 기능 구현
- 성별 수정 기능 구현
- 닉네임 중복 검증 구현
- 회원정보 수정 테스트 작성
- Member.updateProfile() 도입
- Gender 선택 목록 View 리팩토링
  - @ModelAttribute 활용
  - Thymeleaf 반복문 적용

#### Withdrawal

- 회원탈퇴 기능 구현
- Soft Delete 정책 적용
- 회원탈퇴 테스트 작성
- 회원탈퇴 View 연동
- 회원탈퇴 시 세션 무효화 처리

#### Authentication

- LoginCheckInterceptor 구현
- WebConfig 구현
- 인터셉터 기반 인증 처리 적용
- Controller의 수동 로그인 체크 제거
- 로그인 페이지 복귀 URL 처리
- URL Encoding 적용

#### Exception

- DuplicateEmailException 구현
- DuplicateNicknameException 구현
- LoginFailedException 구현
- MemberNotFoundException 구현
- 예외 구조 통일

#### Home

- HomeController 구현
- Home View 구현
- Home CSS 적용
- 로그인 상태에 따른 화면 분기 구현

#### Board

##### Design
- Board 도메인 설계
- BoardStatus 설계
- BoardCategory 설계
- 게시글 권한 정책 설계
- Soft Delete 정책 설계
- 공지사항 정책 설계
- 조회수 정책 설계
- 첨부파일 정책 설계
- Board 관련 문서 작성

---

## In Progress

### Board

- Board Entity 구현

---

## Next Tasks

### Board
- [ ] Board Entity 구현
- [ ] Board Repository 구현
- [ ] Board 생성 기능 구현
- [ ] Board 생성 테스트 작성
- [ ] 게시글 목록 조회 기능 구현
- [ ] 게시글 상세 조회 기능 구현
- [ ] 게시글 수정 기능 구현
- [ ] 게시글 삭제 기능 구현
- [ ] 게시글 조회수 기능 구현

### Attachment

- [ ] UploadFile Entity 구현
- [ ] 파일 업로드 기능 구현
- [ ] 파일 다운로드 기능 구현

### Comment

- [ ] Comment Entity 구현
- [ ] 댓글 기능 구현
- [ ] 대댓글 기능 구현

### Like

- [ ] BoardLike Entity 구현
- [ ] 좋아요 기능 구현

---

## Latest Commit

[ebc6cde] docs: define board domain design

---

## 프로젝트 문서
- [README](../README.md)
  - 프로젝트 소개

- [Domain Model](./domain-model.md)
  - 도메인 모델 및 비즈니스 규칙 정의

- [Database Design](./database-design.md)
  - 데이터베이스 스키마, 관계 및 ERD 정의