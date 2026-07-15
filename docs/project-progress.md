# Project Progress

## Current Branch

feature/comment

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
8. 문서 최신화
9. Git 정리

구현보다 설계와 테스트를 우선하며, Controller 및 View는 Service 계층 검증 이후 작성한다.

기능 규모가 큰 경우, Service/Test 완료 시점과 Controller/View 완료 시점에 중간 커밋을 나누어 진행한다.

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
- Architecture Decisions 문서 작성 및 관리
- ERD 문서화

---

## Config

- WebConfig 구현
- LoginCheckInterceptor 등록
  - 정적 리소스 경로 제외 설정 적용
  - 홈, 회원가입, 로그인, 에러 페이지 접근 제외 설정 적용
- LoginMemberIdArgumentResolver 등록

---

## Authentication

- HttpSession 기반 인증 구조 구현
- LoginCheckInterceptor 구현
- 인터셉터 기반 로그인 검증 적용
- Controller 인증 로직 제거
- URL Encoding 기반 로그인 후 원래 요청 페이지 복귀 처리 적용
- 비회원 게시글 목록 조회 허용
- 비회원 게시글 상세 조회 허용
- 비회원 댓글 목록 조회 허용

---

## ArgumentResolver

- LoginMemberId 어노테이션 구현
- LoginMemberIdArgumentResolver 구현
- Controller에서 로그인 사용자 ID 주입 방식 통일
- Controller의 HttpSession 직접 의존 제거
- 로그인 검증 이후 사용자 ID 누락 시 시스템 불변식 위반으로 처리

---

## Exception

- GlobalExceptionHandler 구현
- AccessDeniedException 구현
- MemberNotFoundException 전역 처리 적용
- BoardNotFoundException 전역 처리 적용
- AccessDeniedException 전역 처리 적용
- HTML 화면 기반 redirect 예외 처리 정책 적용

---

## Home

- HomeController 구현
- Home View 구현
- 로그인 상태에 따른 화면 분기 구현

---

## Member

### Domain

- Member Entity 구현
- Gender 구현
- Role 구현
- MemberStatus 구현
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
- Enum 사용자 친화적 표시 적용

### Profile Update

- 회원정보 수정 기능 구현
- 닉네임 수정 기능 구현
- 성별 수정 기능 구현
- 닉네임 중복 검증 구현
- 회원정보 수정 테스트 작성

### Withdrawal

- 회원탈퇴 기능 구현
- 회원탈퇴 테스트 작성
- 회원탈퇴 시 세션 무효화 처리
- Soft Delete 정책 적용

### Exception

- DuplicateEmailException 구현
- DuplicateNicknameException 구현
- LoginFailedException 구현
- MemberNotFoundException 구현

---

## Board

### Domain

- Board Entity 구현
- BoardCategory 구현
- BoardStatus 구현
- Board Repository 구현

### Write

- 게시글 작성 기능 구현
- 게시글 작성 테스트 작성
- 공지사항 작성 권한 정책 적용

### Read

- 게시글 상세 조회 기능 구현
- 게시글 조회 테스트 작성
- 삭제 게시글 상세 조회 방어
- 게시글 조회수 증가 기능 구현
- Cookie 기반 조회수 중복 증가 방지 적용
- 비회원 게시글 상세 조회 허용

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

#### 페이징 정책

- PAGE_SIZE = 15
- BLOCK_SIZE = 5
- MIN_PAGE = 1

#### 페이지 번호 정책

- page < 1 → 1로 보정(clamp)
- page > totalPages → 마지막 페이지로 보정(clamp)

#### 페이지 이동 정책

- 이전 페이지 이동
- 다음 페이지 이동

### Update

- 게시글 수정 화면 조회 기능 구현
- 게시글 수정 기능 구현
- 게시글 수정 테스트 작성
- 작성자 수정 권한 정책 적용
- 관리자 공지사항 수정 권한 검증

### Delete

- 게시글 삭제 기능 구현
- 게시글 삭제 테스트 작성
- Soft Delete 정책 적용
- 작성자 및 관리자 삭제 권한 정책 적용
- 삭제 확인 메시지(confirm) 적용
- 권한 기반 삭제 버튼 노출

### Exception

- BoardNotFoundException 구현

---

## Comment

### Domain

- Comment Entity 구현
- CommentStatus 구현
- Comment Repository 구현
- Comment Self Reference 구조 적용
- Soft Delete 정책 적용

### Write

- 댓글 작성 기능 구현
- 대댓글 작성 기능 구현
- 댓글 작성 Service 테스트 작성
- ACTIVE 회원 및 ACTIVE 게시글 검증 적용
- 대댓글 1단계 제한 정책 적용
- 삭제된 댓글에도 대댓글 작성 허용
- 다른 게시글의 댓글에 대댓글을 작성할 수 없도록 검증 적용

### Read

- 댓글 목록 조회 기능 구현
- 댓글 조회 Service 테스트 작성
- 댓글과 대댓글 함께 조회
- 삭제 댓글 표시 정책 적용
- 화면 출력 순서에 맞춘 flat list 응답 구성

### AJAX

- CommentApiController 구현
- 댓글 목록 조회 API 구현
- 댓글/대댓글 작성 API 구현
- 게시글 상세 화면 댓글 영역 AJAX 전환
- 댓글 관련 JavaScript/CSS 파일 분리

### Validation

- WriteCommentRequest Bean Validation 적용
- 댓글 내용 필수 입력 및 최대 길이 검증 적용
- API validation 실패 시 JSON 오류 메시지 응답 처리

---

# In Progress

Comment 기능 구현 중

## 현재 완료된 범위

- 댓글/대댓글 작성
- 댓글 목록 조회
- AJAX 기반 댓글 조회/작성

## 현재 남은 범위

- 댓글 수정
- 댓글 삭제
- 댓글 수정/삭제 AJAX 적용 
- API 전용 예외 처리 구조 검토
- 로그인 만료 또는 비로그인 API 요청에 대한 JSON 응답 처리

## 향후 확장 고려 기능

- 댓글 좋아요

---

# Next Tasks

## Comment

- 댓글 수정 기능 구현
- 댓글 삭제 기능 구현
- 댓글 수정/삭제 AJAX 적용
- 댓글 수정/삭제 권한 정책 적용
- 댓글 수정/삭제 구현 후 관련 프로젝트 문서 추가 최신화
- API 전용 예외 처리 구조 검토

## BoardLike

- 게시글 좋아요 기능 구현

## UploadFile

- 파일 업로드 기능 구현
- 파일 다운로드 기능 구현

## Board

- 게시글 정렬 기능
- 게시글 검색 기능
- 마이페이지 내 작성 게시글 조회

## Refactoring

- Spring Data JPA Page/Pageable 기반 페이징 리팩토링 검토
- API 응답 공통 포맷 검토
- HTML 화면 예외 처리와 API 예외 처리 분리 검토

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
