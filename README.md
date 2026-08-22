# Dev Board

Spring Boot 기반 실무형 개발자 커뮤니티 게시판

Dev Board는 개발자들이 게시글과 댓글을 통해 자유롭게 정보와 의견을 나눌 수 있는 커뮤니티 게시판을 구현하며,
백엔드 애플리케이션의 설계와 비즈니스 규칙 검증 과정을 경험하고 기록하기 위해 진행한 개인 프로젝트이다.  
회원가입과 로그인부터 게시글 작성·조회·수정·삭제, 페이징, 조회수 관리, 댓글과 대댓글의 AJAX 기반 CRUD까지 게시판 서비스의 핵심 기능을 구현했다.

단순히 기능을 동작시키는 데 그치지 않고, 실제 서비스 개발 과정에서 중요하게 다뤄지는 계층별 책임 분리, 인증과 권한 검증, 요청 데이터 Validation, 예외 처리, Soft Delete, 테스트 코드 작성 등을 직접 설계하고 적용했다.  
또한, 주요 정책과 기술적 선택의 근거를 문서화하여 코드와 설계 의사결정이 함께 관리되도록 구성했다.

---

# 프로젝트 목표

- 회원, 게시글, 댓글을 중심으로 커뮤니티 서비스의 핵심 기능을 처음부터 끝까지 직접 설계하고 구현한다.
- 기능 구현에 앞서 비즈니스 규칙과 권한 정책을 명확하게 정의하고, 일관된 구조로 코드에 반영한다.
- DTO를 기반으로 계층 간 경계를 분리하고, 각 계층이 자신의 책임에 집중하도록 설계한다.
- 인증, 사용자 식별, 예외 처리와 같은 공통 관심사를 별도의 구조로 분리한다.
- 정상 동작뿐 아니라, 상태와 권한에 따른 실패 상황까지 테스트하여 비즈니스 규칙을 검증한다.
- 주요 정책과 기술적 선택의 배경을 문서화하여 코드와 설계 의사결정을 함께 관리한다.
- 기능 확장과 리팩토링을 고려한 구조를 만들고, 구현 과정에서 발견한 개선점을 지속적으로 기록한다.

---

# 기술 스택

## Backend

- Java 21
- Spring Boot 4.x
- Spring MVC
- Spring Data JPA
- Hibernate

## Database

- MySQL

## Template Engine

- Thymeleaf

## Build Tool

- Gradle

## Test

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
- 미인증 SSR 요청과 API 요청의 응답 정책 분리
- 세션 만료 후 로그인 시 기존 화면 복귀 처리

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
- 삭제 게시글을 `삭제된 게시글입니다.` 문구로 표시

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

- 댓글 및 대댓글 작성
- ACTIVE 회원 및 ACTIVE 게시글 검증
- 대댓글 1단계 제한 정책 적용
- 삭제 댓글에도 대댓글 작성 허용
- 다른 게시글의 댓글에 대댓글을 작성할 수 없도록 검증

### Read

- 댓글 및 대댓글 목록 조회
- 삭제 댓글을 `삭제된 댓글입니다.` 문구로 표시
- 부모 댓글 아래에 대댓글이 표시되는 댓글 구조 제공
- 로그인 회원별 댓글 수정/삭제 가능 여부 제공

### Update

- 댓글 및 대댓글 수정
- ACTIVE 회원/댓글/게시글 검증
- 댓글 작성자만 수정할 수 있도록 권한 정책 적용
- 삭제 댓글의 수정 제한

### Delete

- 댓글 및 대댓글 삭제
- ACTIVE 회원/댓글/게시글 검증
- 댓글 작성자 또는 관리자 삭제 권한 정책 적용
- Soft Delete 정책 적용
- 삭제 댓글의 재삭제 제한

### AJAX

- 댓글 작성/조회/수정/삭제 AJAX 적용
- `CommentApiController` 기반 JSON API 분리
- 댓글별 권한에 따른 수정/삭제 버튼 표시
- 게시글 상세 화면에서 페이지 새로고침 없이 댓글 영역 갱신
- 댓글 수정/삭제 성공 시 `204 No Content` 응답 적용

### Validation

- 댓글 작성/수정 요청에 Bean Validation 적용
- Validation 실패 시 `400 Bad Request`와 `VALIDATION_ERROR` 적용
- JSON 오류 메시지를 댓글 작성/수정 Form에 표시
- 서버 Validation과 클라이언트 사전 검증 역할 분리

---

# 아키텍처 특징

## Controller, Service, Entity의 책임 분리

`Controller`는 요청 데이터 검증과 응답 처리에 집중하고, 구체적인 비즈니스 흐름은 `Service`에 위임한다.  
`Service`는 하나의 유스케이스를 수행하며, 회원과 대상 데이터의 상태 검증, `Repository` 조회, 권한 검사 및 도메인 객체 간 협력 조율을 담당한다.  
`Entity`는 자신의 상태와 도메인 규칙을 책임지며, 상태를 변경하는 의미 있는 메서드를 통해 유효한 상태 전이만 허용한다.  
이를 통해 각 계층의 책임을 명확하게 구분하고, 비즈니스 규칙이 `Controller`와 `Service`에 중복되거나,
`Entity`의 내부 상태가 외부에서 임의로 변경되는 것을 방지한다.  
또한 웹 계층과 분리된 상태에서 유스케이스를 테스트하고, `Entity`의 상태 변경 규칙을 일관되게 유지할 수 있다.

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

이 구조는 Presentation Layer가 JPA Entity에 직접 의존하지 않도록 하여, View가 영속성 모델의 내부 구조와 변경에 영향받는 범위를 줄이기 위함이다.  
View에는 화면에 필요한 데이터만 DTO로 전달하므로 불필요한 필드와 민감 정보의 노출을 방지할 수 있으며,
화면 요구사항에 따른 데이터가 Entity에 추가되는 것을 막아 도메인 모델의 책임과 캡슐화를 유지할 수 있다.

또한 OSIV(false) 환경에서 영속성 컨텍스트의 사용 범위를 Service 계층으로 제한하고, 필요한 데이터는 Service에서 DTO로 변환하여 View에 전달한다.  
이를 통해 View 렌더링 시점의 Lazy Loading 의존을 제거하고, `LazyInitializationException`을 예방한다.

---

## 인증 책임 분리

로그인 여부 검증과 로그인 회원 식별을 Controller의 비즈니스 처리와 분리한다.

### LoginCheckInterceptor

로그인이 필요한 요청에 대해 Controller 실행 전, 세션의 로그인 여부를 검증한다.  
이를 통해 각 Controller에서 로그인 검증 코드를 반복하지 않고,
인증이 필요한 경로의 접근 정책을 한 곳에서 관리한다.

또한 실행 대상 Handler의 `@RestController`와 `@ResponseBody` 적용 여부를 기준으로
SSR 요청과 API 요청을 구분한다.  
미인증 SSR 요청은 로그인 페이지로의 `302 Found` redirect를 적용하고,
미인증 API 요청은 `LOGIN_REQUIRED` 오류 정보를 담은 JSON을 `401 Unauthorized` 상태로 응답한다.

SSR 요청의 로그인 후 복귀 URL은 서버에서 구성하고,
API 요청은 브라우저 주소창의 현재 페이지를 기준으로 클라이언트에서 복귀 URL을 구성한다.

### @LoginMemberId ArgumentResolver

세션에 저장된 로그인 회원 ID를 해석하여 Controller의 파라미터에 주입한다.  
Controller가 `HttpSession`의 구조와 조회 방식에 직접 의존하지 않으므로,
사용자 식별 코드의 중복을 줄이고 요청 처리 로직에 집중할 수 있다.

---

## SSR / API Exception Handling

예외가 발생한 요청 유형에 따라 HTML 화면과 JSON API의 응답 정책을 분리한다.

- `SsrExceptionHandler`
  - SSR Controller에서 발생한 도메인 및 권한 예외 처리
  - 기존 HTML 화면 기반 redirect 정책 유지
  - HTTP 상태 및 전용 오류 화면 정책은 추후 검토

- `ApiExceptionHandler`
  - API Controller에서 발생한 예외 처리
  - `ApiErrorCode`와 `ApiErrorResponse` 기반의 일관된 JSON 오류 응답 제공

요청 형식 오류, Validation 실패, 도메인 및 권한 예외를 공통 처리하여
각 Controller가 비즈니스 요청과 정상 응답 처리에 집중하도록 구성했다.

API 오류 응답의 세부 형식과 오류 코드 목록은 [API Specification](./docs/api-specification.md) 문서를 참고한다.

---

## Soft Delete

데이터를 물리적으로 제거하지 않고, 상태값을 변경하여 삭제 처리한다.  
이를 통해 삭제 이력을 보존하고, 회원·게시글·댓글 사이의 기존 연관 관계가 데이터 삭제로 인해 유실되는 것을 방지한다.

조회, 수정, 삭제 등의 유스케이스에서는 상태값을 검증하여 삭제된 데이터의 사용을 제한하고,
화면에서 각 도메인의 정책에 따라 삭제 상태를 반영한다.

적용 대상

- Member
- Board
- Comment

---

## Pagination

게시판 페이징 계산 로직을 `BoardPagingUtils`로 분리한다.  
현재는 게시판 페이징 정책을 명시적으로 제어하고 검증하기 위해 Spring Data JPA의 `Page`와 `Pageable`을 바로 사용하지 않고 자체 구현 방식을 적용했다.

이를 통해 다음과 같은 기능을 직접 설계하고 구현했다.

- 전체 게시글 수 계산
- 전체 페이지 수 계산
- 페이지 블록 계산
- 이전/다음 페이지 이동
- Clamp 기반 페이지 보정

### 정책

- `PAGE_SIZE` = 15
- `BLOCK_SIZE` = 5
- `MIN_PAGE` = 1

### 페이지 보정

- `page < 1` → 첫 번째 페이지
- `page > totalPages` → 마지막 페이지

### 향후 개선 계획

현재 구현은 페이징 요구사항과 계산 기준을 코드와 테스트로 명확히 검증하기 위한 초기 구현이다.  
추후 Spring Data JPA의 `Page`와 `Pageable`을 활용하는 방향으로 리팩토링을 진행하여, 직접 구현한 방식과 프레임워크가 제공하는 방식의 차이점 및 장단점을 비교/분석할 예정이다.

---

## Comment API / AJAX

댓글 기능은 게시글 상세 화면의 일부 영역만 갱신되는 특성을 고려하여 AJAX 기반으로 구현했다.  
게시글 상세 화면은 Thymeleaf View로 렌더링하고, 댓글 조회·작성·수정·삭제는 JSON API로 처리한다.

### 요청 처리 구조

HTML View와 JSON API의 책임을 다음과 같이 분리한다.

- `BoardController`: 게시글 상세 HTML View 반환
- `CommentApiController`: 댓글 조회·작성·수정·삭제 JSON API 처리

댓글 변경에 성공하면 목록 전체를 다시 조회하여 서버의 최신 정렬 순서, 삭제 상태 및 로그인 회원의 수정/삭제 권한을 화면에 일관되게 반영한다.

- 댓글 작성 성공: `201 Created`
- 댓글 수정·삭제 성공: `204 No Content`
- 댓글 작성·수정 Validation 실패: `400 Bad Request` + `VALIDATION_ERROR` 오류 코드

### 응답 및 오류 처리

댓글 조회·작성·수정·삭제 요청에 공통 API 응답 처리 방식을 적용하여
성공, API 오류 및 네트워크 연결 실패에 일관되게 대응한다.

API 요청에 실패하면 서버가 반환한 오류 메시지를 우선 표시하고,
정상적인 JSON 오류 응답을 받을 수 없는 경우에는 요청별 fallback 메시지를 사용한다.  
JSON이 아닌 오류 응답과 네트워크 연결 실패도 공통 오류 처리 흐름에 포함하여
브라우저의 기술적인 오류가 사용자에게 직접 노출되지 않도록 한다.

### 인증 만료 처리

세션이 만료된 상태에서 인증이 필요한 댓글 API를 요청하면
`LOGIN_REQUIRED` 오류 코드와 `401 Unauthorized` 응답을 감지하여 로그인 페이지로 이동한다.  
로그인 후에는 기존 게시글 상세 화면으로 복귀하지만, 사용자의 의도와 중복 실행 가능성을 고려하여
중단된 댓글 변경 요청은 자동으로 재실행하지 않는다.

---

## 권한 정책

게시글과 댓글은 수정과 삭제의 성격이 다르다고 판단하여, 서로 다른 권한 정책을 적용한다.  
수정은 작성자의 의사와 표현을 변경하는 작업이므로 작성자에게만 허용한다.  
삭제는 서비스 운영과 관리 목적을 고려하여, 작성자뿐 아니라 관리자에게도 허용한다.

### 게시글

- 수정: 작성자만 가능
- 삭제: 작성자 또는 관리자 가능

### 댓글

- 수정: 작성자만 가능
- 삭제: 작성자 또는 관리자 가능

자세한 설계 배경은 Architecture Decisions 문서를 참고한다.

---

# 테스트 전략

Dev Board는 기능의 정상 동작만 확인하는 데 그치지 않고,
도메인 상태와 사용자 권한에 따른 실패 흐름까지 검증하는 것을 목표로 한다.

## 비즈니스 규칙 검증

Service 테스트에서는 회원, 게시글과 댓글의 상태에 따른 유스케이스 수행 가능 여부를 검증한다.  
작성자와 관리자의 수정/삭제 권한, 공지사항 작성 권한, 대댓글 단계 제한, 삭제 데이터의 사용 제한 등
주요 비즈니스 규칙이 정상 흐름과 예외 상황에서 일관되게 적용되는지 확인한다.

게시글 페이징처럼 계산 규칙이 포함된 기능은
첫 페이지와 마지막 페이지, 페이지 블록 경계, 범위를 벗어난 페이지 보정 등 경계값을 함께 검증한다.

## 웹 요청 및 API 응답 검증

웹 계층에서는 미인증 SSR 요청과 API 요청이 각각 redirect와 JSON 오류 응답으로 분기되는지 검증한다.

API 예외 처리 테스트에서는 잘못된 JSON, 타입 불일치, Validation 실패, 도메인 및 권한 예외가
정해진 HTTP 상태와 `ApiErrorCode`, 오류 메시지 및 JSON 응답 형식으로 변환되는지 확인한다.  
이를 통해 예외 처리 구현뿐 아니라, 서버와 클라이언트 사이의 오류 응답 계약이 유지되는지도 함께 검증한다.

## 테스트 환경 및 브라우저 검증

개발 환경과 테스트 환경의 데이터베이스를 분리하여
테스트 실행이 개발 데이터에 영향을 주지 않도록 구성한다.

자동화 테스트 이후에는 브라우저 개발자 도구 등을 활용하여
댓글의 정상 처리, Validation 실패, 인증 만료, 도메인 및 권한 오류,
JSON이 아닌 응답과 네트워크 연결 실패까지 확인한다.  
이를 통해 서버의 응답뿐 아니라, 실제 화면에 표시되는 메시지와 로그인 후 페이지 복귀 등
클라이언트의 최종 동작도 함께 검증한다.

---

# 개발 프로세스

Dev Board는 다음 순서로 기능을 구현한다.

```text
1. 기능 설계
2. DTO 작성
3. Service 작성
4. Service Test 작성
5. Controller 작성
6. View 작성
7. 브라우저 수동 테스트
8. 문서 최신화
9. Git 정리
```

기능 구현에 앞서 정책과 구조를 설계하고, 테스트를 통해 정상·실패 흐름 및 비즈니스 규칙을 검증한다.

---

# 현재 진행 상황

## 구현 완료

### Member

- 회원가입
- 로그인 / 로그아웃
- 마이페이지
- 회원정보 수정
- 회원탈퇴

### 인증 & 권한

- `LoginCheckInterceptor`
- `LoginMemberIdArgumentResolver`
- 세션 기반 인증
- 관리자 권한 정책
- 미인증 SSR/API 요청의 응답 정책 분리
- 세션 만료 후 로그인 시 기존 화면 복귀 처리

### Board

- 게시글 작성
- 게시글 상세 조회
- 게시글 목록 조회
  - 삭제 게시글 안내 문구 표시
- 비회원 게시글 조회 허용
- 게시글 페이징
- 게시글 수정
- 게시글 삭제
- 게시글 조회수
- 공지사항 시각적 강조

### Comment

- 댓글 및 대댓글 작성
- 댓글 및 대댓글 목록 조회
  - 삭제 댓글 안내 문구 표시
- 댓글 및 대댓글 수정
- 댓글 및 대댓글 삭제
- 댓글 작성/조회/수정/삭제 AJAX 적용
- 로그인 회원별 수정/삭제 권한 제공
- 대댓글 1단계 제한
- 댓글 작성/수정 Validation 적용

### 공통 인프라

- SSR/API 전용 예외 처리기 분리
- `ApiErrorCode`와 `ApiErrorResponse` 기반 API 오류 응답 표준화
- 요청 형식, Validation, 도메인 및 권한 예외 공통 처리
- 테스트 DB 분리
- Soft Delete 정책 적용
- `WebConfig` 기반 Interceptor 및 ArgumentResolver 등록

---

## 진행 중

### Documentation

- API Specification 문서 추가
- API 오류 응답 구조와 클라이언트 처리 정책 문서화
- SSR/API 예외 처리 및 Validation 관련 Architecture Decisions 갱신
- 세션 만료 Troubleshooting에 후속 오류 응답 표준화 과정 반영
- README, Domain Design 및 Project Progress 최신화
- 프로젝트 문서 간 정책·용어·링크 일치 여부 검토
- 전체 변경 사항 및 테스트 결과 최종 확인
- 문서 변경 사항 커밋 및 Git 정리

---

## 진행 예정

### 커뮤니티

- 게시글 좋아요 기능
- 댓글 좋아요 기능

### 파일

- 파일 업로드 기능
- 파일 다운로드 기능

### 게시글

- 게시글 정렬 기능
- 게시글 검색 기능
- 마이페이지 내 작성 게시글 조회

### 리팩토링

- Spring Data JPA Page/Pageable 기반 페이징 리팩토링 검토

---

# 프로젝트 문서

- [Domain Design](./docs/domain-design.md)
  - 도메인 모델 및 비즈니스 규칙 정의

- [Database Design](./docs/database-design.md)
  - 데이터베이스 스키마, 관계 및 ERD 정의

- [API Specification](./docs/api-specification.md)
  - JSON API의 공통 요청·응답 규칙 및 오류 코드 명세

- [Architecture Decisions](./docs/architecture-decisions.md)
  - 주요 아키텍처 설계 의사결정 기록

- [Project Progress](./docs/project-progress.md)
  - 현재 프로젝트 진행 현황 및 개발 계획

- [Troubleshooting](./docs/troubleshooting.md)
  - 개발 과정에서 발생한 주요 문제의 원인 분석 및 해결 과정 기록
