# API Specification

## 개요

본 문서는 Dev Board에서 제공하는 JSON API의 공통 요청·응답 규칙과 오류 응답 명세를 정의한다.

현재는 공통 API 오류 응답과 오류 코드 명세를 중심으로 관리하며,
향후 API가 확장되면 엔드포인트별 요청·성공 응답 명세를 추가한다.

SSR 방식으로 HTML View를 반환하는 웹 요청은 본 문서의 대상에 포함하지 않는다.

---

## 공통 응답 규칙

### 성공 응답

API 요청이 성공하면 각 엔드포인트의 목적에 맞는 HTTP 상태를 반환한다.

현재 댓글 API는 다음 성공 상태를 사용한다.

|     요청      |     HTTP 상태      |   응답 본문    |
|:-----------:|:----------------:|:----------:|
|  댓글 목록 조회   |     `200 OK`     | 댓글 목록 JSON |
| 댓글 및 대댓글 작성 |  `201 Created`   |     없음     |
| 댓글 및 대댓글 수정 | `204 No Content` |     없음     |
| 댓글 및 대댓글 삭제 | `204 No Content` |     없음     |

---

### 오류 응답

본 문서에서 정의한 API 오류는 HTTP 상태와 함께 다음 형식의 JSON을 반환한다.

```json
{
  "code": "BOARD_NOT_FOUND",
  "message": "게시글을 찾을 수 없습니다."
}
```

|    필드     |   타입   | 설명                                   |
|:---------:|:------:|:-------------------------------------|
|  `code`   | String | 클라이언트가 오류를 식별하고 처리하기 위한 애플리케이션 오류 코드 |
| `message` | String | 사용자에게 안내할 수 있는 오류 메시지                |

HTTP 상태와 애플리케이션 오류 코드는 서로 다른 역할을 가진다.

- HTTP 상태는 요청 처리 결과의 표준적인 의미를 나타낸다.
- `code`는 같은 HTTP 상태에 포함될 수 있는 세부 오류를 구분한다.
- `message`는 사용자에게 표시할 수 있는 기본 안내 문구를 제공한다.

클라이언트의 동작을 결정하는 분기는 `message` 문자열이 아니라 HTTP 상태와 `code`를 기준으로 한다.

---

## API 오류 코드

|           코드            |           HTTP 상태           | 기본 메시지                  | 주요 발생 상황                  |
|:-----------------------:|:---------------------------:|:------------------------|:--------------------------|
|    `INVALID_REQUEST`    |      `400 Bad Request`      | 요청 형식이 올바르지 않습니다.       | JSON 형식 오류 또는 요청 값 타입 불일치 |
|   `VALIDATION_ERROR`    |      `400 Bad Request`      | 입력값이 올바르지 않습니다.         | Bean Validation 실패        |
|    `LOGIN_REQUIRED`     |     `401 Unauthorized`      | 로그인이 필요합니다.             | 인증이 필요한 API의 미인증 요청       |
|     `ACCESS_DENIED`     |       `403 Forbidden`       | 해당 요청을 처리할 권한이 없습니다.    | 요청을 수행할 권한이 없는 경우         |
|   `MEMBER_NOT_FOUND`    |       `404 Not Found`       | 회원을 찾을 수 없습니다.          | 회원을 찾을 수 없는 경우            |
|    `BOARD_NOT_FOUND`    |       `404 Not Found`       | 게시글을 찾을 수 없습니다.         | 게시글을 찾을 수 없는 경우           |
|   `COMMENT_NOT_FOUND`   |       `404 Not Found`       | 댓글을 찾을 수 없습니다.          | 댓글을 찾을 수 없는 경우            |
|   `REPLY_NOT_ALLOWED`   |       `409 Conflict`        | 해당 댓글에 대댓글을 작성할 수 없습니다. | 대댓글 작성 정책과 충돌하는 경우        |
| `INTERNAL_SERVER_ERROR` | `500 Internal Server Error` | 서버 오류가 발생했습니다.          | 별도로 처리되지 않은 서버 오류         |

---

## 요청 형식 오류

요청 본문을 읽을 수 없거나 요청 값의 타입이 일치하지 않으면 다음 오류를 반환한다.

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json
```

```json
{
  "code": "INVALID_REQUEST",
  "message": "요청 형식이 올바르지 않습니다."
}
```

현재 다음 오류가 포함된다.

- JSON 문법 오류로 요청 본문을 읽을 수 없는 경우
- Path Variable 등 요청 값의 타입이 Controller 파라미터 타입과 일치하지 않는 경우

---

## Validation 오류

요청 DTO의 Bean Validation에 실패하면 다음 오류를 반환한다.

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json
```

```json
{
  "code": "VALIDATION_ERROR",
  "message": "검증 오류 메시지"
}
```

Validation 오류 메시지는 다음 정책을 따른다.

- 검증 오류가 존재하면 그중 하나의 메시지를 응답한다.
- 여러 검증 오류가 발생하더라도 하나의 메시지만 반환한다.
- 특정 필드의 오류가 우선된다는 순서는 보장하지 않는다.
- 사용할 수 있는 검증 메시지가 없으면 `VALIDATION_ERROR`의 기본 메시지를 반환한다.
  - 기본 메시지: `입력값이 올바르지 않습니다.`

클라이언트 사전 검증은 불필요한 요청을 줄이고 빠르게 사용자에게 안내하기 위해 사용한다.  
요청 데이터의 유효성을 최종적으로 보장하는 책임은 서버에 있다.

---

## 인증 실패

인증이 필요한 API를 미인증 상태로 요청하면 로그인 페이지로 redirect하지 않고 다음 오류를 반환한다.

```http
HTTP/1.1 401 Unauthorized
Content-Type: application/json;charset=UTF-8
```

```json
{
  "code": "LOGIN_REQUIRED",
  "message": "로그인이 필요합니다."
}
```

미인증 상태에는 다음 경우가 포함된다.

- 처음부터 로그인하지 않은 경우
- 로그인 후 세션이 만료된 경우

클라이언트는 HTTP 상태가 `401 Unauthorized`이고 `code`가 `LOGIN_REQUIRED`인 경우에만 로그인 필요 상황으로 처리한다.

---

## 권한 오류

로그인한 사용자가 해당 요청을 수행할 권한이 없으면 다음 오류를 반환한다.

```http
HTTP/1.1 403 Forbidden
Content-Type: application/json
```

```json
{
  "code": "ACCESS_DENIED",
  "message": "해당 요청을 처리할 권한이 없습니다."
}
```

---

## 서버 오류

별도로 처리되지 않은 서버 예외가 발생하면 내부 예외 정보를 응답에 노출하지 않고 다음 오류를 반환한다.

```http
HTTP/1.1 500 Internal Server Error
Content-Type: application/json
```

```json
{
  "code": "INTERNAL_SERVER_ERROR",
  "message": "서버 오류가 발생했습니다."
}
```

원본 예외와 Stack Trace는 클라이언트 응답에 포함하지 않고 서버 로그에 기록한다.

---

## 댓글 클라이언트 오류 처리

현재 댓글 클라이언트는 API 요청에서 다음 기준으로 오류를 처리한다.

- 정상적인 API 오류 응답이면 서버가 반환한 `message`를 사용자에게 표시한다.
- `401 Unauthorized`와 `LOGIN_REQUIRED`가 함께 반환되면 로그인 페이지로 이동한다.
- 오류 응답이 JSON 형식이 아니거나 메시지가 없으면 요청별 fallback 메시지를 표시한다.
- 네트워크 오류로 응답을 받지 못한 경우에도 요청별 fallback 메시지를 표시한다.
- 사용자에게는 안내 메시지를 표시하고, 처리 중 발생한 오류는 브라우저 콘솔에 기록한다.

---

## 향후 확장

향후 JSON API가 확장되면 다음 내용을 본 문서에 추가한다.

- 엔드포인트별 HTTP 메서드와 경로
- 인증 필요 여부
- Path Variable 및 Query Parameter
- 요청 본문 필드와 Validation 규칙
- 성공 응답의 상태와 본문
- 엔드포인트별 발생 가능한 오류 코드
- 요청 및 응답 예시

---

## 프로젝트 문서

- [README](../README.md)
  - 프로젝트 소개

- [Domain Design](./domain-design.md)
  - 도메인 모델 및 비즈니스 규칙 정의

- [Database Design](./database-design.md)
  - 데이터베이스 스키마, 관계 및 ERD 정의

- [Architecture Decisions](./architecture-decisions.md)
  - 주요 아키텍처 설계 의사결정 기록

- [Project Progress](./project-progress.md)
  - 현재 프로젝트 진행 현황 및 개발 계획

- [Troubleshooting](./troubleshooting.md)
  - 주요 문제의 원인 분석 및 해결 과정 기록
