# Troubleshooting

Dev Board를 개발하면서 발견한 문제의 증상, 재현 과정, 원인 분석, 해결 과정과 검증 결과를 기록한다.

각 사례는 문제를 해결한 결과만 나열하지 않고, 어떤 근거를 통해 원인을 좁혔고,
왜 해당 해결 방법을 선택했는지까지 함께 남기는 것을 목적으로 한다.

---

## 1. AJAX 댓글 변경 요청에서 세션 만료를 정상적으로 처리하지 못하는 문제

### 상태

진행 중

### 문제 발견 배경

댓글 작성·수정·삭제 기능을 AJAX 방식으로 구현한 뒤,
게시글 상세 페이지를 열어 둔 상태에서 로그인 세션이 만료되면 댓글 변경 요청이 어떻게 처리되는지 점검했다.

일반적인 화면 요청은 미인증 상태에서 로그인 페이지로 이동시키는 방식이 자연스럽지만,
`fetch()`를 사용하는 API 요청에도 동일한 redirect 정책을 적용하면
클라이언트가 인증 실패를 올바르게 인식하지 못할 수 있다고 판단했다.

### 재현 환경 및 조건

- 게시글 상세 페이지에 로그인 상태로 접근한다.
- 브라우저 개발자 도구를 연다.
- Application 탭에서 `JSESSIONID` 쿠키만 삭제해 세션 만료 상태를 만든다.
- 화면은 새로고침하지 않아 로그인 상태에서 렌더링된 댓글 작성·수정·삭제 UI를 유지한다.
- Network 탭의 `Preserve log`를 활성화한다.
- 댓글 작성, 수정, 삭제 요청을 각각 독립적으로 실행한다.

### 예상했던 문제

`LoginCheckInterceptor`가 미인증 API 요청에도 로그인 페이지로의 `302 Found` 응답을 반환하고,
`fetch()`가 redirect를 자동으로 따라가면서 최초 요청의 인증 실패가 클라이언트에 그대로 전달되지 않을 것으로 예상했다.

요청 메서드에 따라 redirect 이후의 동작과 최종 증상은 다를 수 있다고 보았다.

### 실제 재현 결과

|       구분       |                       댓글 작성                       |                         댓글 수정                          |                            댓글 삭제                             |
|:--------------:|:-------------------------------------------------:|:------------------------------------------------------:|:------------------------------------------------------------:|
|     최초 요청      |            `POST /boards/103/comments`            |                  `PATCH /comments/7`                   |                     `DELETE /comments/4`                     |
|     최초 응답      |                    `302 Found`                    |                      `302 Found`                       |                         `302 Found`                          |
|  redirect 대상   | `/members/login?redirectURL=/boards/103/comments` |        `/members/login?redirectURL=/comments/7`        |           `/members/login?redirectURL=/comments/4`           |
| redirect 후 메서드 |                       `GET`                       |                        `PATCH`                         |                           `DELETE`                           |
|     최종 응답      |               `200 OK`, 로그인 폼 HTML                |             `405 Method Not Allowed`, JSON             |                `405 Method Not Allowed`, JSON                |
|  사용자에게 나타난 증상  |        작성 성공으로 오인하여 입력창을 비우고 댓글 목록을 다시 조회함        | `Method 'PATCH' is not supported.`라는 기술적 오류를 수정 폼에 표시함 | `Method 'DELETE' is not supported.`라는 기술적 오류를 `alert()`로 표시함 |

#### 댓글 작성 요청

```text
POST /boards/103/comments
→ 302 Found
→ GET /members/login?redirectURL=/boards/103/comments
→ 200 OK, text/html
```

`fetch()`는 redirect를 자동으로 따라간 뒤 최종 응답만 반환했다.  
클라이언트는 로그인 폼 HTML과 함께 받은 최종 `200 OK`를 댓글 작성 성공으로 오인했다.

그 결과 댓글은 실제로 저장되지 않았지만 다음 성공 후 처리가 실행됐다.

- 댓글 입력창 초기화
- 댓글 목록 재조회
- 세션 만료 안내 누락

#### 댓글 수정 요청

```text
PATCH /comments/7
→ 302 Found
→ PATCH /members/login?redirectURL=/comments/7
→ 405 Method Not Allowed, application/json
```

redirect 이후에도 `PATCH` 메서드가 유지됐다.  
로그인 Controller는 `GET`과 `POST`만 지원하므로 최종적으로 `405 Method Not Allowed`가 발생했다.

클라이언트는 JSON 응답의 `message`를 읽어 다음과 같은 Spring의 기술적 오류 문구를 사용자에게 그대로 표시했다.

```text
Method 'PATCH' is not supported.
```

#### 댓글 삭제 요청

```text
DELETE /comments/4
→ 302 Found
→ DELETE /members/login?redirectURL=/comments/4
→ 405 Method Not Allowed, application/json
```

수정 요청과 마찬가지로 redirect 이후에도 `DELETE` 메서드가 유지됐으며, 로그인 URL에서 `405 Method Not Allowed`가 발생했다.

클라이언트는 다음 기술적 오류 문구를 `alert()`로 표시했다.

```text
Method 'DELETE' is not supported.
```

### 원인 분석

근본 원인은 `LoginCheckInterceptor`가 화면을 반환하는 SSR 요청과 JSON 기반 API 요청을 구분하지 않고,
모든 미인증 요청에 로그인 페이지로의 `302 Found`를 반환하는 데 있다.

```text
미인증 댓글 변경 API 요청
→ LoginCheckInterceptor에서 로그인 페이지 redirect
→ fetch()가 redirect 자동 추적
→ 최초 인증 실패가 최종 응답 뒤에 가려짐
→ 클라이언트가 인증 만료 상황을 정상적으로 판단하지 못함
```

요청별 증상은 다르지만 모두 같은 인증 처리 정책에서 비롯됐다.

- `POST`는 redirect 과정에서 `GET`으로 변경되어 로그인 폼의 `200 OK`를 받았다.
- `PATCH`와 `DELETE`는 메서드가 유지되어 로그인 URL에서 `405 Method Not Allowed`가 발생했다.
- Interceptor가 생성한 `redirectURL`은 사용자가 돌아가야 할 게시글 상세 페이지가 아니라 API URL을 가리켰다.

### 환경에 따라 달라질 수 있는 관찰 결과

`PATCH`와 `DELETE`의 최종 오류 본문이 JSON이었던 점과 응답에 stack trace가 포함된 점은 이번 문제의 근본 원인이 아니다.

- `405` 오류는 Spring MVC에서 발생하고 Spring Boot의 기본 오류 처리기를 통해 JSON 응답으로 변환됐다.
- 개발 환경에서는 DevTools의 오류 관련 기본 설정에 의해 `message`와 stack trace가 응답에 포함될 수 있다.
- 오류 응답 형식과 내부 정보 노출 정책은 별도의 오류 응답 표준화 리팩터링에서 다룬다.

### 해결 방향 및 설계 쟁점

현재 검토 중이며, 구현 전 다음 사항을 결정한다.

- SSR 요청과 API 요청을 구분하는 기준
- 미인증 API 요청에 반환할 HTTP 상태와 응답 형식
- 클라이언트의 공통 인증 만료 처리 방식
- 로그인 후 복귀할 화면 URL을 결정하는 주체
- 인증 실패로 중단된 변경 요청의 자동 재실행 여부

현재 검토 중인 기본 방향은 다음과 같다.

```text
미인증 SSR 요청
→ 로그인 페이지로 302 redirect

미인증 API 요청
→ 401 Unauthorized
→ 클라이언트가 현재 게시글 상세 URL을 redirectURL로 구성
→ 로그인 페이지로 이동
```

이 방향은 아직 구현 및 검증 전이므로 최종 결정으로 기록하지 않는다.

### 최종 해결 내용

> 수정 완료 후 실제 구현 결과를 기준으로 작성한다.

### 테스트 및 재검증

> 자동 테스트와 브라우저 재검증을 완료한 뒤 작성한다.

### 회고

> 해결 및 검증을 마친 뒤 문제 해결 과정에서 얻은 점과 향후 적용할 원칙을 작성한다.
