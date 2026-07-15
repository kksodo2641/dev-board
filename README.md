# Dev Board

Spring Boot 기반 실무형 개발자 커뮤니티 게시판

Dev Board는 단순 CRUD 구현을 넘어, 
실제 서비스에서 요구되는 계층 분리, 인증/인가, 예외 처리, 데이터 보존 정책을
직접 설계하고 코드와 문서로 검증하는 프로젝트이다.

---

# 프로젝트 목표

- 계층 간 역할과 책임 분리
- DTO 기반 계층 분리
- 도메인 중심 설계
- 테스트 코드 기반 기능 검증
- 인터셉터 기반 인증 처리
- ArgumentResolver 기반 로그인 사용자 식별
- 전역 예외 처리 구조 적용
- Soft Delete 정책 적용
- 설계 의사결정 문서화

---

# 기술 스택

### Backend

- Java 21
- Spring Boot 4.x
- Spring MVC
- Spring Data JPA
- Hibernate

### Database

- MySQL

### Template Engine

- Thymeleaf

### Build Tool

- Gradle

### Test

- JUnit 5
- AssertJ
- Spring Boot Test

---

# 구현 기능

## Member

### 인증(Authentication)

- 회원가입
- 로그인
- 로그아웃
- 세션 기반 인증

### 마이페이지

- 회원정보 조회
- 회원정보 수정
- 회원탈퇴

### 권한(Authorization)

- 사용자(USER)
- 관리자(ADMIN)

권한에 따라 일부 기능 접근을 제한한다.

---

## Board

### Write

- 게시글 작성
- 공지사항 작성 권한 정책 적용

### Read

- 게시글 상세 조회
- 비회원 게시글 조회 허용
- 게시글 조회수
- Cookie 기반 조회수 중복 증가 방지

### List

- 게시글 목록 조회
- 공지사항 시각적 강조
- 삭제 게시글 표시 지원

### Paging

- 게시글 페이징
- 페이지 블록 계산
- Clamp 기반 페이지 보정

### Update

- 게시글 수정
- 작성자 수정 권한 정책 적용

### Delete

- 게시글 삭제
- 작성자 및 관리자 삭제 권한 정책 적용

---

## Comment

### Write

- 댓글 작성
- 대댓글 작성
- ACTIVE 회원 및 ACTIVE 게시글 검증
- 대댓글 1단계 제한 정책 적용
- 삭제된 댓글에도 대댓글 작성 허용

### Read

- 댓글 목록 조회
- 댓글과 대댓글 함께 조회
- 삭제 댓글 표시 지원
- 부모 댓글 아래에 대댓글이 표시되는 댓글 구조 제공

### AJAX

- 댓글 목록 AJAX 조회
- 댓글/대댓글 AJAX 작성
- CommentApiController 기반 JSON API 분리
- 게시글 상세 화면에서 페이지 새로고침 없이 댓글 영역 갱신

---

# 아키텍처 특징

## Service와 Entity의 책임 분리

Service는 하나의 유스케이스를 수행한다.

사용자 요청 검증, Repository 조회, 권한 검사, 도메인 객체 간 협력 조율을 담당한다.

Entity는 자신의 상태와 도메인 규칙을 책임지며, 상태 전이를 최종적으로 보호한다.

---

## DTO 기반 계층 분리

Controller와 Service는 DTO를 통해 데이터를 전달한다.

Entity를 View 계층에 직접 노출하지 않는다.

Service는 DTO를 통해 요청을 받고, Repository와는 Entity를 통해 데이터를 주고받는다.

```text
Controller
 ↕
DTO
 ↕
Service
 ↕
Entity
 ↕
Repository
```

이 구조는 Presentation Layer가 JPA Entity에 직접 의존하지 않도록 하기 위함이다.

또한 OSIV(false) 환경에서 영속성 컨텍스트의 사용 범위를 Service 계층으로 제한하고, 필요한 데이터는 Service에서 DTO로 변환하여 View에 전달한다.

이를 통해 View 렌더링 시점의 Lazy Loading 의존을 제거하고, LazyInitializationException을 예방한다.

---

## Service 중심 비즈니스 로직

비즈니스 규칙은 Service 계층에서 처리한다.

Controller는 요청/응답 처리에 집중한다.

예시

- ACTIVE 회원 검증
- 게시글 작성 권한 검증
- 게시글 수정 권한 검증
- 게시글 삭제 권한 검증
- 공지사항 작성/수정 권한 검증
- 댓글 작성 시 ACTIVE 회원 검증
- 댓글 작성 시 ACTIVE 게시글 검증
- 대댓글 1단계 제한 검증

---

## 인증 책임 분리

### LoginCheckInterceptor

로그인 여부를 검증한다.

### @LoginMemberId ArgumentResolver

로그인 사용자 ID를 Controller에 주입한다.

Controller에서 HttpSession을 직접 다루지 않는다.

---

## Global Exception Handling

GlobalExceptionHandler를 통해 예외 처리 정책을 통합한다.

적용 예외

- MemberNotFoundException
- BoardNotFoundException
- AccessDeniedException

---

## Soft Delete

실제 데이터를 삭제하지 않고 상태값을 변경한다.

삭제 데이터의 이력과 연관 관계를 유지하여 데이터 무결성과 서비스 일관성을 보장한다.

적용 대상

- Member
- Board
- Comment

---

## Pagination

게시판 페이징 계산 로직을 BoardPagingUtils로 분리한다.

현재는 게시판 페이징 정책을 명시적으로 제어하고 검증하기 위해 
Spring Data JPA의 `Page`와 `Pageable`을 바로 사용하지 않고 자체 구현 방식을 적용했다.

이를 통해 다음과 같은 기능을 직접 설계하고 구현했다.

- 전체 게시글 수 계산
- 전체 페이지 수 계산
- 페이지 블록 계산
- 이전/다음 페이지 이동
- Clamp 기반 페이지 보정

### 정책

- PAGE_SIZE = 15
- BLOCK_SIZE = 5
- MIN_PAGE = 1

### 페이지 보정

- page < 1 → 첫 번째 페이지
- page > totalPages → 마지막 페이지

### 향후 개선 계획

현재 구현은 페이징 요구사항과 계산 기준을 코드와 테스트로 명확히 검증하기 위한 초기 구현이다.

추후 Spring Data JPA의 `Page`와 `Pageable`을 활용하는 방향으로 리팩토링을 진행하여, 
직접 구현한 방식과 프레임워크가 제공하는 방식의 차이점 및 장단점을 비교/분석할 예정이다.

---

## Comment API / AJAX

댓글 기능은 게시글 상세 화면의 일부 영역만 갱신되는 특성을 가지므로, AJAX 기반으로 구현한다.

게시글 상세 화면 자체는 Thymeleaf View로 렌더링하고, 댓글 목록 조회와 댓글/대댓글 작성은 JSON API로 처리한다.

이를 위해 Controller 역할을 다음처럼 분리한다.

```text
BoardController
 → 게시글 상세 HTML View 반환

CommentApiController
 → 댓글 목록 조회 및 댓글/대댓글 작성 JSON API 처리
```

현재 적용 범위

- 댓글 목록 조회
- 댓글 작성
- 대댓글 작성

향후 적용 예정

- 댓글 수정
- 댓글 삭제

---

## 권한 정책

게시글과 댓글은 수정과 삭제에 서로 다른 권한 정책을 적용한다.

### 게시글

- 수정: 작성자만 가능
- 삭제: 작성자 또는 관리자 가능

### 댓글

- 수정: 작성자만 가능
- 삭제: 작성자 또는 관리자 가능

자세한 설계 배경은 Architecture Decisions 문서를 참고한다.

---

# 테스트 전략

Dev Board는 기능 검증보다 비즈니스 규칙 검증에 초점을 둔다.

예시

- 회원가입 중복 검증
- 로그인 정책 검증
- 공지사항 작성 권한 검증
- 게시글 수정/삭제 권한 검증
- 게시글 페이징 정책 검증
- 페이지 보정(clamp) 정책 검증
- 댓글 작성 정책 검증
- 대댓글 1단계 제한 검증
- 삭제 댓글 표시 정책 검증
- 댓글 조회 순서 검증

또한 개발 환경과 테스트 환경을 분리하여 테스트를 수행한다.

---

# 개발 프로세스

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

구현보다 설계와 테스트를 우선한다.

---

# 현재 진행 상황

### 구현 완료

#### Member

- 회원가입
- 로그인 / 로그아웃
- 마이페이지
- 회원정보 수정
- 회원탈퇴

#### 인증 & 권한

- LoginCheckInterceptor
- LoginMemberId ArgumentResolver
- 세션 기반 인증
- 관리자 권한 정책

#### Board

- 게시글 작성
- 게시글 상세 조회
- 게시글 목록 조회
- 비회원 게시글 조회 허용
- 게시글 페이징
- 게시글 수정
- 게시글 삭제
- 게시글 조회수
- 공지사항 시각적 강조

#### Comment

- 댓글 작성
- 대댓글 작성
- 댓글 목록 조회
- AJAX 기반 댓글 조회
- AJAX 기반 댓글/대댓글 작성
- 삭제 댓글 표시
- 대댓글 1단계 제한

#### 공통 인프라

- GlobalExceptionHandler
- 테스트 DB 분리
- Soft Delete 정책 적용
- WebConfig 기반 Interceptor 및 ArgumentResolver 등록

---

### 진행 중

#### Comment

- 댓글 수정
- 댓글 삭제
- 댓글 수정/삭제 AJAX 적용
- API 전용 예외 처리 구조 검토

---

### 진행 예정

#### 커뮤니티

- 게시글 좋아요 기능
- 댓글 좋아요 기능

#### 파일

- 파일 업로드 기능
- 파일 다운로드 기능

#### 게시판

- 게시글 정렬 기능
- 게시글 검색 기능
- 마이페이지 내 작성 게시글 조회

#### 리팩토링

- Spring Data JPA Page/Pageable 기반 페이징 리팩토링 및 비교 분석
- HTML 화면 예외 처리와 API 예외 처리 분리 검토
- API 응답 공통 포맷 검토

---

## 프로젝트 문서

- [Domain Design](./docs/domain-design.md)
  - 도메인 모델 및 비즈니스 규칙 정의

- [Database Design](./docs/database-design.md)
  - 데이터베이스 스키마, 관계 및 ERD 정의

- [Architecture Decisions](./docs/architecture-decisions.md)
  - 주요 아키텍처 설계 의사결정 기록

- [Project Progress](./docs/project-progress.md)
  - 현재 프로젝트 진행 현황 및 개발 계획
