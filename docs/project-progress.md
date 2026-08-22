# Project Progress

## 개요

본 문서는 Dev Board 프로젝트의 개발 진행 현황과 향후 계획을 기록하기 위한 문서이다.  
완료된 작업, 현재 브랜치에서 진행 중인 작업, 다음 기능 및 리팩토링 계획을 구분하여
프로젝트의 현재 상태와 이후 진행 방향을 한눈에 확인할 수 있도록 관리한다.

### 참고

- 별도의 구분이 필요한 경우를 제외하고, `댓글`은 `일반 댓글`과 `대댓글`을 모두 포함한다.

---

## 현재 브랜치

`refactor/api-error-handling`

---

## 개발 프로세스

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

구현보다 설계와 테스트를 우선하며, Controller 및 View는 Service 계층 검증 이후 작성한다.

기능 규모가 큰 경우, Service/Test 완료 시점과 Controller/View 완료 시점에 중간 커밋을 나누어 진행한다.

---

## 완료

### Common

- `dev_board` 데이터베이스 생성
- `schema.sql` 작성 및 관리
- Spring Boot 프로젝트 초기 설정
- Git / GitHub 연동
- `BaseTimeEntity` 구현
- JPA Auditing 적용
- `dev_board_test` 테스트 전용 데이터베이스 생성
- 테스트 전용 데이터베이스 환경 분리

---

### Documentation

- README 작성
- Domain Design 작성
- Database Design 작성
- API Specification 작성 및 관리
- Project Progress 작성 및 관리
- Architecture Decisions 작성 및 관리
- Troubleshooting 작성 및 관리
- ERD 문서화

---

### Config

- `WebConfig` 구현
- `LoginCheckInterceptor` 등록
  - 정적 리소스 경로 제외 설정 적용
  - 홈, 회원가입, 로그인, 에러 페이지 접근 제외 설정 적용
  - `@RestController` 및 `@ResponseBody` 기반 API 요청 판별 적용
- `LoginMemberIdArgumentResolver` 등록

---

### Authentication

- `HttpSession` 기반 인증 구조 구현
- `LoginCheckInterceptor` 구현
- 인터셉터 기반 로그인 검증 적용
- Controller 인증 로직 제거
- URL Encoding 기반 로그인 후 원래 요청 페이지 복귀 처리 적용
- 미인증 SSR 요청과 API 요청의 응답 정책 분리
- 미인증 SSR 요청에 로그인 페이지 `302 Found` redirect 적용
- 미인증 API 요청에 `LOGIN_REQUIRED` JSON을 `401 Unauthorized` 상태로 응답
- 미인증 SSR/API 요청의 응답 분기 테스트 작성
- 미인증 API 응답의 상태, 오류 코드, 메시지 및 응답 형식 검증
- SSR 요청의 로그인 후 복귀 URL을 서버에서 구성
- API 요청의 로그인 후 복귀 URL을 클라이언트에서 구성
- 인증 실패로 중단된 댓글 변경 요청의 자동 재실행 제한
- 비회원 게시글 목록 조회 허용
- 비회원 게시글 상세 조회 허용
- 비회원 댓글 목록 조회 허용

---

### ArgumentResolver

- `@LoginMemberId` 어노테이션 구현
- `LoginMemberIdArgumentResolver` 구현
- Controller에서 로그인 사용자 ID 주입 방식 통일
- Controller의 `HttpSession` 직접 의존 제거
- 로그인 검증 이후 사용자 ID 누락 시 시스템 불변식 위반으로 처리

---

### Exception

- `AccessDeniedException` 구현
- 기존 `GlobalExceptionHandler`를 SSR/API 전용 예외 처리기로 분리
- `SsrExceptionHandler` 구현
  - SSR Controller로 적용 범위 제한
  - `MemberNotFoundException` 전역 처리
  - `BoardNotFoundException` 전역 처리
  - `AccessDeniedException` 전역 처리
  - 기존 HTML 화면 기반 redirect 정책 유지
- `ApiExceptionHandler` 구현
  - `@RestController` 기반 API 예외 처리
  - 요청 형식, Validation, 도메인, 권한 및 서버 오류 공통 처리
  - 처리되지 않은 예외의 내부 정보 비노출 및 서버 로그 기록
- `ApiErrorCode`와 `ApiErrorResponse` 기반 공통 API 오류 응답 적용
- HTTP 상태와 애플리케이션 오류 코드를 이용한 오류 식별 기준 적용
- API 예외 처리의 주요 응답 분기 테스트 작성

---

### Home

- `HomeController` 구현
- Home View 구현
- 로그인 상태에 따른 화면 분기 구현

---

### Member

#### Domain

- `Member` 엔티티 구현
- `Gender` Enum 구현
- `Role` Enum 구현
- `MemberStatus` Enum 구현
- `MemberRepository` 구현

#### Signup

- 회원가입 기능 구현
- `BCryptPasswordEncoder` 적용
- 이메일 중복 검증 구현
- 닉네임 중복 검증 구현
- 회원가입 테스트 작성

#### Login / Logout

- 로그인 기능 구현
- 로그아웃 기능 구현
- `HttpSession` 기반 로그인 구현
- 로그인 테스트 작성

#### MyPage

- 마이페이지 조회 기능 구현
- Enum 사용자 친화적 표시 적용

#### Profile Update

- 회원정보 수정 기능 구현
- 닉네임 수정 기능 구현
- 성별 수정 기능 구현
- 닉네임 중복 검증 구현
- 회원정보 수정 테스트 작성

#### Withdrawal

- 회원탈퇴 기능 구현
- 회원탈퇴 테스트 작성
- 회원탈퇴 시 세션 무효화 처리
- Soft Delete 정책 적용

#### Exception

- `DuplicateEmailException` 구현
- `DuplicateNicknameException` 구현
- `LoginFailedException` 구현
- `MemberNotFoundException` 구현

---

### Board

#### Domain

- `Board` 엔티티 구현
- `BoardCategory` Enum 구현
- `BoardStatus` Enum 구현
- `BoardRepository` 구현

#### Write

- 게시글 작성 기능 구현
- 게시글 작성 테스트 작성
- 공지사항 작성 권한 정책 적용

#### Read

- 게시글 상세 조회 기능 구현
- 게시글 조회 테스트 작성
- 삭제 게시글 상세 조회 방어
- 게시글 조회수 증가 기능 구현
- Cookie 기반 조회수 중복 증가 방지 적용
- 비회원 게시글 상세 조회 허용

#### List

- 게시글 목록 조회 기능 구현
- 게시글 목록 조회 테스트 작성
- 삭제 게시글 목록 표시 지원
- 비회원 게시글 목록 조회 허용
- 공지사항 시각적 강조 적용

#### Paging

- `BoardPageResponse` 구현
- `BoardPagingRepository` 구현
- `BoardPagingRepositoryImpl` 구현
- `BoardPagingUtils` 구현
- 게시글 페이징 조회 기능 구현
- 게시글 페이징 테스트 작성
- 게시글 목록 화면 페이징 UI 구현

##### 페이징 정책

- `PAGE_SIZE` = 15
- `BLOCK_SIZE` = 5
- `MIN_PAGE` = 1

##### 페이지 번호 정책

- `page < 1` → 1로 보정(clamp)
- `page > totalPages` → 마지막 페이지로 보정(clamp)

##### 페이지 이동 정책

- 이전 페이지 이동
- 다음 페이지 이동

#### Update

- 게시글 수정 화면 조회 기능 구현
- 게시글 수정 기능 구현
- 게시글 수정 테스트 작성
- 작성자 수정 권한 정책 적용
- 관리자 공지사항 수정 권한 검증

#### Delete

- 게시글 삭제 기능 구현
- 게시글 삭제 테스트 작성
- Soft Delete 정책 적용
- 작성자 및 관리자 삭제 권한 정책 적용
- 삭제 확인 메시지(confirm) 적용
- 권한 기반 삭제 버튼 노출

#### Exception

- `BoardNotFoundException` 구현

---

### Comment

#### Domain

- `Comment` 엔티티 구현
- `CommentStatus` Enum 구현
- `CommentRepository` 구현
- Comment Self Reference 구조 적용

#### Write

- 댓글 작성 기능 구현
- 댓글 작성 Service 테스트 작성
- `ACTIVE` 회원 및 `ACTIVE` 게시글 검증 적용
- 대댓글 1단계 제한 정책 적용
- 삭제된 댓글에도 대댓글 작성 허용
- 다른 게시글의 댓글에 대댓글을 작성할 수 없도록 검증 적용

#### Read

- 댓글 목록 조회 기능 구현
- 댓글 조회 Service 테스트 작성
- 댓글과 대댓글 함께 조회
- 삭제 댓글 표시 정책 적용
- 로그인 회원별 댓글 수정/삭제 가능 여부 제공
- 화면 출력 순서에 맞춘 flat list 응답 구성

#### Update

- 댓글 수정 기능 구현
- 댓글 수정 Service 테스트 작성
- `ACTIVE` 회원/댓글/게시글 검증 적용
- 댓글 작성자만 수정할 수 있도록 권한 정책 적용
- 관리자도 다른 회원의 댓글을 수정할 수 없도록 제한
- 삭제된 댓글 수정 제한

#### Delete

- 댓글 삭제 기능 구현
- 댓글 삭제 Service 테스트 작성
- `ACTIVE` 회원/댓글/게시글 검증 적용
- 댓글 작성자 또는 관리자만 삭제할 수 있도록 권한 정책 적용
- Soft Delete 정책 적용
- 삭제된 댓글 재삭제 제한

#### AJAX

- `CommentApiController` 구현
- 댓글 목록 조회 API 구현
- 댓글 작성 API 구현
- 댓글 수정 API 구현
- 댓글 삭제 API 구현
- 게시글 상세 화면의 댓글 작성·조회·수정·삭제 AJAX 전환
- 댓글 작성 성공 시 `201 Created` 응답 적용
- 댓글 수정·삭제 성공 시 `204 No Content` 응답 적용
- 댓글 변경 요청 성공 후 댓글 목록 전체 재조회 적용
- 댓글별 수정/삭제 가능 여부에 따른 수정/삭제 버튼 표시
- 댓글 관련 JavaScript/CSS 파일 분리
- 인증 만료 시 현재 게시글 상세 페이지를 로그인 후 복귀 URL로 전달
- 인증 실패 시 성공 후 처리와 댓글 목록 재조회 중단
- 댓글 조회·작성·수정·삭제 요청의 API 응답 처리 공통화
- `LOGIN_REQUIRED` 오류 코드 기반 인증 만료 처리 적용
- 서버가 반환한 오류 메시지와 요청별 fallback 메시지 처리
- JSON이 아닌 오류 응답 및 네트워크 연결 실패 대응

#### Validation

- `WriteCommentRequest`, `UpdateCommentRequest`에 Bean Validation 적용
- `MethodArgumentNotValidException` 기반 공통 API Validation 처리
- Validation 실패에 `400 Bad Request`와 `VALIDATION_ERROR` 적용
- DTO의 Validation 메시지 및 기본 fallback 메시지 적용
- JSON 오류 메시지를 댓글 작성/수정 Form에 표시
- 서버 Validation과 클라이언트 사전 검증의 역할 분리

---

## 진행 중

API 오류 응답 표준화 리팩터링 관련 문서 최신화

### 현재 브랜치 작업 범위

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

### BoardLike

- 게시글 좋아요 기능 구현

### CommentLike

- 댓글 좋아요 기능 구현

### UploadFile

- 파일 업로드 기능 구현
- 파일 다운로드 기능 구현

### Board

- 게시글 정렬 기능
- 게시글 검색 기능
- 마이페이지 내 작성 게시글 조회

### Refactoring

- Spring Data JPA Page/Pageable 기반 페이징 리팩토링 검토

#### 도메인별 안정화 및 리팩토링 검토 계획

아래 항목은 기능 구현 과정에서 발견한 잠재적인 문제와 개선 대상을 정리한 것이다.

실제 수정 전에는 로그, 재현, 테스트 등을 통해 문제를 먼저 확인하고 분석한다.

서로 관련 없는 변경은 하나의 브랜치에 포함하지 않고,
책임과 목적에 따라 브랜치를 분리하여 진행한다.

##### Board 검토 항목

- 게시글 목록 DTO 변환 시, 작성자 LAZY 조회로 인한 N+1 쿼리 가능성 검토
  - 문제 확인 시 fetch join, DTO projection 등 대안 비교
- 존재하지 않거나 삭제된 게시글을 검증하기 전에, 조회수 Cookie를 응답에 추가하는 처리 순서 개선
- 상세 조회할 수 없는 삭제 게시글의 목록 링크 활성화 문제 개선
- 게시글 상세 화면에서 목록 복귀 시, 기존 페이지 상태가 사라지는 문제 개선
- 조회수 증가 요청 동시 발생 시, 갱신 유실 가능성 검토
- 하나의 `viewedBoards` Cookie에 게시글 ID가 계속 누적되는 구조의 크기 제한 검토

##### Global / Member 검토 항목

- `HomeController`가 `MemberRepository`와 `MemberStatus`에 직접 의존하는 구조를 Service 기반으로 개선
- 로그인 `redirectURL`이 애플리케이션 내부 경로인지 검증하는 방안 검토
- datasource 접속 정보를 환경변수 또는 외부 설정으로 분리
- 향후 Spring Security 도입 시, 세션 기반 상태 변경 요청의 CSRF 방어 적용 검토

##### Comment 검토 항목

- 댓글 목록 DTO 변환 시, 작성자 LAZY 조회로 인한 N+1 쿼리 가능성 검토
- 댓글 수 증가 시 댓글 페이징 도입 검토
- 삭제 댓글의 작성자 및 작성일시 표시 정책 확정
- AJAX 요청 중 중복 제출을 방지하기 위한 버튼 비활성화 검토
- 댓글 개수에 대댓글을 포함할지 표시 정책 명확화
- 댓글 관계 데이터의 정합성이 깨진 경우에도, 조회 순서 조립 과정이 안전하도록 방어 로직 검토

### Documentation

- 별도 문서 정제 브랜치에서 Architecture Decisions 전체 검토
  - 초기 ADR의 배경, 대안, 선택 이유 및 트레이드오프 보강
  - 후속 ADR과의 중복 및 참조 관계 정리
  - 지나치게 긴 ADR의 분량 축소 및 가독성 개선
  - 최종 구현과 설계 결정의 일치 여부 확인

---

## 프로젝트 문서

- [README](../README.md)
  - 프로젝트 소개

- [Domain Design](./domain-design.md)
  - 도메인 모델 및 비즈니스 규칙 정의

- [Database Design](./database-design.md)
  - 데이터베이스 스키마, 관계 및 ERD 정의

- [API Specification](./api-specification.md)
  - JSON API의 공통 요청·응답 규칙 및 오류 코드 명세

- [Architecture Decisions](./architecture-decisions.md)
  - 주요 설계 의사결정 기록

- [Troubleshooting](./troubleshooting.md)
  - 개발 과정에서 발생한 주요 문제의 원인 분석 및 해결 과정 기록
