# Project Progress

## Current Branch

feature/board

---

# Development Process

Dev Board는 다음 순서로 기능을 구현한다.

1. 기능 설계
2. DTO 작성
3. Service 작성
4. Service Test 작성
5. Controller 작성
6. View 작성
7. 브라우저 수동 테스트

구현보다 설계와 테스트를 우선하며, Controller 및 View는 Service 계층 검증 이후 작성한다.

---

# Completed

## Common

- dev_board 데이터베이스 생성
- schema.sql 작성 및 관리
- Spring Boot 프로젝트 초기 설정
- Git / GitHub 연동
- BaseTimeEntity 구현
- JPA Auditing 적용
- dev_board_test 테스트 전용 데이터베이스 생성
- 테스트 전용 데이터베이스 환경 분리

---

## Documentation

- README 작성
- Domain Design 문서 작성
- Database Design 문서 작성
- Project Progress 문서 작성 및 관리

---

## Member

### Domain

- Member Entity 구현
- Gender Enum 구현
- Role Enum 구현
- MemberStatus Enum 구현
- Member Repository 구현

### Signup

- 회원가입 기능 구현
- BCrypt PasswordEncoder 적용
- 이메일 중복 검증 구현
- 닉네임 중복 검증 구현
- 회원가입 테스트 작성

### Login / Logout

- 로그인 기능 구현
- 로그아웃 기능 구현
- HttpSession 기반 로그인 구현
- 로그인 테스트 작성

### MyPage

- 마이페이지 조회 기능 구현
- 로그인 후 원래 요청 페이지 복귀(redirectURL) 구현
- Enum 사용자 친화적 표시 적용

### Profile Update

- 회원정보 수정 기능 구현
- 닉네임 수정 기능 구현
- 성별 수정 기능 구현
- 닉네임 중복 검증 구현
- 회원정보 수정 테스트 작성

### Withdrawal

- 회원탈퇴 기능 구현
- Soft Delete 정책 적용
- 회원탈퇴 테스트 작성
- 회원탈퇴 시 세션 무효화 처리

### Authentication

- LoginCheckInterceptor 구현
- 인터셉터 기반 인증 처리 적용
- Controller 인증 로직 제거
- URL Encoding 기반 로그인 복귀 처리 적용

### Exception

- DuplicateEmailException 구현
- DuplicateNicknameException 구현
- LoginFailedException 구현
- MemberNotFoundException 구현
- BoardNotFoundException 구현
- AccessDeniedException 구현
- 예외 구조 통일
- GlobalExceptionHandler 적용

### Home

- HomeController 구현
- Home View 구현
- 로그인 상태에 따른 화면 분기 구현

---

## Board

### Domain

- Board Entity 구현
- BoardCategory 구현
- BoardStatus 구현
- 게시글 Soft Delete 정책 적용
- 게시글 상태 전이 방어 로직 적용

### Write

- 게시글 작성 기능 구현
- 게시글 작성 테스트 작성
- 공지사항 작성 권한 정책 적용

### Read

- 게시글 상세 조회 기능 구현
- BoardNotFoundException 구현
- 게시글 조회 테스트 작성
- 삭제 게시글 상세 조회 방어
- 게시글 조회수 증가 기능 구현
- Cookie 기반 조회수 중복 증가 방지 적용

### List

- 게시글 목록 조회 기능 구현
- 게시글 목록 조회 테스트 작성
- 삭제 게시글 목록 표시 지원
- 비회원 게시글 목록 조회 허용
- 공지사항 시각적 강조 적용

### Paging

- BoardPageResponse 구현
- BoardPagingRepository 구현
- BoardPagingRepositoryImpl 구현
- BoardPagingUtils 구현
- 게시글 페이징 조회 기능 구현
- 게시글 페이징 테스트 작성
- 게시글 목록 화면 페이징 UI 구현

페이징 정책

- PAGE_SIZE = 15
- BLOCK_SIZE = 5
- MIN_PAGE = 1

페이지 번호 정책

- page < 1 → 1로 보정(clamp)
- page > totalPages → 마지막 페이지로 보정(clamp)

페이지 이동 정책

- 이전 페이지 이동
- 다음 페이지 이동
- 블록 이동 방식 미적용

### Update

- 게시글 수정 화면 조회 기능 구현
- 게시글 수정 기능 구현
- 게시글 수정 테스트 작성
- 작성자 수정 권한 정책 적용
- 관리자 공지사항 수정 권한 검증

### Delete

- 게시글 삭제 기능 구현
- 게시글 삭제 테스트 작성
- 작성자 및 관리자 삭제 권한 정책 적용
- 삭제 확인 메시지(confirm) 적용
- 권한 기반 삭제 버튼 노출

---

# In Progress

게시판 핵심 기능 구현 완료

---

# Next Tasks

- 댓글(Comment)
- 게시글 좋아요(BoardLike)
- 파일 업로드(UploadFile)
- 게시글 정렬 기능
- 게시글 검색 기능

---

# Architecture Highlights

- DTO 기반 계층 분리
- Service 중심 비즈니스 로직 처리
- LoginCheckInterceptor 기반 인증 처리
- LoginMemberId ArgumentResolver 기반 로그인 사용자 식별
- GlobalExceptionHandler 기반 전역 예외 처리
- Soft Delete 정책 적용
- ACTIVE 회원 검증 책임 Service에 집중
- 테스트 코드 기반 기능 검증

---

## 프로젝트 문서

- [README](../README.md)
    - 프로젝트 소개

- [Domain Design](./domain-design.md)
    - 도메인 모델 및 비즈니스 규칙 정의

- [Database Design](./database-design.md)
    - 데이터베이스 스키마, 관계 및 ERD 정의

- [Architecture Decisions](./architecture-decisions.md)
    - 주요 설계 의사결정 기록
