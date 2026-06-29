# Dev Board

Spring Boot 기반 실무형 개발자 커뮤니티 게시판

Dev Board는 단순 CRUD 구현을 넘어, 실제 서비스에서 고려해야 하는 설계 원칙과 아키텍처를 학습하고 적용하기 위해 개발 중인 프로젝트다.

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

# 아키텍처 특징

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

---

## Pagination

게시판 페이징 계산 로직을 BoardPagingUtils로 분리한다.

현재는 페이징의 동작 원리를 직접 이해하고 구현하기 위해 Spring Data JPA의 `Page`와 `Pageable`을 사용하지 않고 자체 구현 방식을 적용했다.

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

현재 구현은 페이징의 내부 동작을 학습하기 위한 목적의 구현이다.

추후 Spring Data JPA의 `Page`와 `Pageable`을 활용하는 방향으로 리팩토링을 진행하여, 직접 구현한 방식과 프레임워크가 제공하는 방식의 차이점 및 장단점을 비교·분석할 예정이다.

---

## 권한 정책

게시글 수정과 삭제는 서로 다른 권한 정책을 적용한다.

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

#### 공통 인프라

- GlobalExceptionHandler
- 테스트 DB 분리
- Soft Delete 정책 적용

---

### 진행 예정

#### 커뮤니티

- 댓글 기능
- 게시글 좋아요 기능

#### 파일

- 파일 업로드 기능

#### 게시판

- 게시글 정렬 기능
- 게시글 검색 기능

#### 리팩토링

- Spring Data JPA Page/Pageable 기반 페이징 리팩토링 및 비교 분석

[//]: # (## 실행 방법)

---

## 프로젝트 문서

- [Domain Design](docs/domain-design.md)
  - 도메인 모델 및 비즈니스 규칙 정의

- [Database Design](./docs/database-design.md)
  - 데이터베이스 스키마, 관계 및 ERD 정의

- [Architecture Decisions](./docs/architecture-decisions.md)
  - 주요 아키텍처 설계 의사결정 기록

- [Project Progress](./docs/project-progress.md)
  - 현재 프로젝트 진행 현황 및 개발 계획
