# Troubleshooting

## 개요

본 문서는 Dev Board를 개발하면서 발견한 문제의 증상, 재현 과정, 원인 분석,
해결 과정과 검증 결과를 기록하기 위한 문서이다.

각 사례는 문제를 해결한 결과만 나열하지 않고, 어떤 근거를 통해 원인을 좁혔고,
왜 해당 해결 방법을 선택했는지까지 함께 남기는 것을 목적으로 한다.

### 참고

- 별도의 구분이 필요한 경우를 제외하고, `댓글`은 `일반 댓글`과 `대댓글`을 모두 포함한다.

---

## 1. AJAX 댓글 변경 요청에서 세션 만료를 정상적으로 처리하지 못하는 문제

### 상태

해결 완료

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

`PATCH`와 `DELETE`의 최종 오류 본문이 JSON이었던 점과, 응답에 Stack Trace가 포함된 점은 이번 문제의 근본 원인이 아니다.

- `405 Method Not Allowed` 오류는 Spring MVC에서 발생하고 Spring Boot의 기본 오류 처리기를 통해 JSON 응답으로 변환됐다.
- 개발 환경에서는 DevTools의 오류 관련 기본 설정에 의해 `message`와 Stack Trace가 응답에 포함될 수 있다.
- 이후 API 오류 응답 표준화 리팩터링을 통해 공통 처리 대상의 오류 응답을 통일하고, 처리되지 않은 API 예외의 내부 정보가 클라이언트에 노출되지 않도록 개선했다.

### 해결 과정 및 설계 결정

#### SSR 요청과 API 요청의 미인증 처리 분리

미인증 요청을 모두 로그인 페이지로 redirect하는 기존 방식에서,
화면을 요청하는 `SSR 요청`과 JSON 기반 `API 요청`을 구분하여 처리하도록 변경했다.

```text
미인증 SSR GET 요청
→ 로그인 페이지로 302 Found
→ 서버가 원래 요청 URL을 redirectURL 쿼리 파라미터의 값으로 전달
→ 로그인 성공 후 redirectURL로 복귀

미인증 SSR 상태 변경 요청
→ redirectURL 없이 로그인 페이지로 302 Found
→ 로그인 성공 후 홈 화면으로 이동

미인증 API 요청
→ 빈 본문과 401 Unauthorized
→ 클라이언트가 인증 만료 상황을 처리
```

`SSR 요청`은 사용자가 브라우저에서 직접 화면을 요청하므로, 로그인 페이지로 이동시키는 기존 방식을 유지한다.

반면 `fetch()`를 사용하는 `API 요청`에 redirect를 반환하면, 클라이언트가 최초 인증 실패 응답이 아닌 redirect 이후의 최종 응답을 받게 된다.  
따라서 미인증 `API 요청`에는 로그인 페이지 HTML이나 별도의 오류 본문을 반환하지 않고,
빈 본문과 `401 Unauthorized`를 반환하도록 결정했다.

#### API 요청 판별 기준

`LoginCheckInterceptor`에서 현재 요청을 처리할 Handler가 `HandlerMethod`인 경우,
Controller 클래스 또는 Handler 메서드에 `@ResponseBody`가 적용되어 있는지 확인한다.

`@RestController`는 `@ResponseBody`를 포함하는 메타 애너테이션이므로
`@RestController` 기반 API와 메서드 단위의 `@ResponseBody` API를 같은 기준으로 판별할 수 있다.

API 여부를 판별할 때 Handler가 `HandlerMethod`가 아니거나
`@ResponseBody`가 적용되지 않은 경우에는 API 요청으로 간주하지 않는다.  
단, `ResourceHttpRequestHandler`가 처리하는 정적 리소스 요청은
이 판별보다 먼저 로그인 검사 없이 통과시킨다.

#### 로그인 후 복귀할 URL 결정

SSR GET 요청에서는 서버가 실제로 요청받은 URI와 쿼리 스트링을 이용해
로그인 후 복귀할 `redirectURL`을 구성한다.  
SSR 상태 변경 요청에는 복귀 URL을 전달하지 않으며,
로그인 성공 후 기존 요청을 자동으로 다시 실행하지 않는다.

API 요청의 URL은 댓글 작성·수정·삭제를 수행하는 엔드포인트이므로,
로그인 후 사용자가 돌아가야 할 화면 주소로서 사용할 수 없다.  
따라서 API 요청에서는 복귀 URL을 서버가 만드는 대신,
현재 화면을 알고 있는 클라이언트에서 현재 게시글 상세 페이지의 주소를 전달하도록 결정했다.

클라이언트는 다음 값을 조합해 현재 화면의 경로와 쿼리 스트링을 보존한다.

```javascript
window.location.pathname + window.location.search
```

이 주소를 로그인 페이지의 `redirectURL` 쿼리 파라미터로 전달해
로그인 성공 후 원래 게시글 상세 페이지로 복귀하도록 했다.

#### 중단된 댓글 요청의 자동 재실행 여부

인증 실패로 중단된 댓글 작성·수정·삭제 요청은 로그인 성공 후 자동으로 재실행하지 않기로 결정했다.

자동 재실행을 지원하려면 요청 종류와 대상, 입력 내용 등을 별도로 보존하고
로그인 후 해당 상태를 안전하게 복원해야 한다.  
또한 사용자가 로그인 후에도 같은 변경 작업을 원하는지 확인하지 않은 채
이전 요청을 다시 전송하면, 의도하지 않은 데이터 변경이 발생할 수 있다.

따라서 로그인 후에는 원래 게시글로 복귀만 시키고 댓글 변경 작업은 사용자가 다시 실행하도록 했다.

#### API 인증 실패 응답의 후속 표준화

빈 본문의 `401 Unauthorized` 응답으로 redirect 문제는 해결했지만,
인증 실패 응답만 다른 API 오류와 형식이 다르다는 한계가 남았다.

이후 `ApiErrorCode`와 `ApiErrorResponse` 기반의 공통 API 오류 응답을 도입하면서
미인증 API 응답도 다음 형식으로 확장했다.

```json
{
  "code": "LOGIN_REQUIRED",
  "message": "로그인이 필요합니다."
}
```

HTTP 상태는 기존과 같이 `401 Unauthorized`를 유지한다.

클라이언트는 단순히 `401 Unauthorized`만 확인하지 않고,
HTTP 상태와 `LOGIN_REQUIRED` 오류 코드를 함께 확인하여 로그인 필요 상황을 판단한다.

댓글 조회·작성·수정·삭제 요청에 공통 API 응답 처리 함수를 적용했다.  
서버가 반환한 오류 메시지를 우선 사용하고,
JSON이 아닌 오류 응답이나 네트워크 실패에는 요청별 fallback 메시지를 표시한다.

구체적인 오류 응답 명세는 [API Specification](./api-specification.md)에서 관리한다.

### 최종 해결 내용

서버에서는 `LoginCheckInterceptor`가 미인증 요청 유형에 따라 다음과 같이 처리한다.

- SSR 요청은 로그인 페이지로 redirect
- SSR GET 요청의 URI와 쿼리 스트링을 로그인 후 복귀 URL로 보존
- SSR 상태 변경 요청은 복귀 URL 없이 로그인 페이지로 이동
- `@RestController` 또는 `@ResponseBody` 기반 API 요청에는 `401 Unauthorized` 반환
- 미인증 API 응답에는 `LOGIN_REQUIRED` 코드와 메시지를 포함하는 JSON 반환
- API 인증 실패 응답에도 공통 `ApiErrorCode`와 `ApiErrorResponse` 사용

클라이언트에서는 댓글 API 요청의 응답을 공통 처리한다.

- `401 Unauthorized`와 `LOGIN_REQUIRED`가 함께 반환되면 세션 만료 상황으로 판단
- 사용자에게 로그인 정보 만료 안내
- 현재 게시글 상세 페이지 주소를 로그인 후 복귀 URL로 전달
- 인증 실패 이후 입력 초기화와 댓글 목록 재조회 등 성공 처리 중단
- 로그인 후 중단된 댓글 요청을 자동으로 재실행하지 않음
- 일반 API 오류는 서버가 반환한 메시지 표시
- JSON이 아닌 오류 응답 또는 네트워크 실패에는 요청별 fallback 메시지 표시

최종 요청 흐름은 다음과 같다.

```text
댓글 변경 요청
→ LoginCheckInterceptor에서 미인증 API 요청 판별
→ 401 Unauthorized와 LOGIN_REQUIRED JSON 응답
→ 클라이언트에서 HTTP 상태와 오류 코드 확인
→ 세션 만료 안내 후 로그인 페이지로 이동
→ 로그인 성공 후 원래 게시글 상세 화면 복귀
```

### 테스트 및 재검증

#### 자동 테스트

`LoginCheckInterceptor` 단위 테스트를 통해 다음 동작을 검증했다.

- `@PublicAccess`가 적용된 요청의 비로그인 접근 허용
- 로그인 세션은 존재하지만, 회원 ID가 없는 보호된 요청 차단
- 로그인 사용자의 보호된 요청 허용
- 미인증 SSR 요청의 로그인 페이지 redirect
  - GET 요청은 복귀 URL 전달
  - 상태 변경 요청은 복귀 URL 없음
- `@RestController` 기반 API 요청 판별
- 메서드 단위의 `@ResponseBody` 기반 API 요청 판별
- 미인증 `POST`, `PATCH`, `DELETE` API 요청에 대한 `401 Unauthorized` 응답
- 미인증 API 응답의 `LOGIN_REQUIRED` 오류 코드 및 메시지
- 미인증 API 응답의 JSON Content-Type과 UTF-8 문자 인코딩

#### 브라우저 재검증

기존 재현 조건과 동일하게 게시글 상세 페이지를 열어둔 상태에서
`JSESSIONID` 쿠키를 삭제한 뒤 댓글 변경 요청을 다시 실행했다.

댓글 작성·수정·삭제 요청에서 다음 동작을 모두 확인했다.

- 각 요청이 redirect 대신 `401 Unauthorized`와 `LOGIN_REQUIRED` 응답을 받는다.
- 세션 만료 안내가 표시된다.
- 로그인 페이지로 이동한다.
- 로그인 성공 후 원래 게시글 상세 페이지로 복귀한다.
- 인증 실패로 중단된 댓글 변경 요청은 자동으로 재실행되지 않는다.

기존에 발생했던 `POST` 요청에서의 로그인 폼 HTML의 성공 응답 오인 및
`PATCH`, `DELETE` 요청의 `405 Method Not Allowed` 오류도 더 이상 발생하지 않았다.

후속 API 오류 처리 리팩터링에서는 다음 흐름도 추가로 확인했다.

- Validation 및 도메인·권한 오류에 서버 메시지가 표시됨
- JSON이 아닌 오류 응답에 요청별 fallback 메시지가 표시됨
- 브라우저를 Offline 상태로 전환 시 네트워크 오류 대신 사용자 안내 메시지가 표시됨
- 네트워크 복구 후 댓글 요청이 다시 정상적으로 처리됨

### 회고

이번 문제를 통해 브라우저의 일반 화면 요청과 `fetch()` 기반 API 요청에는
서로 다른 미인증 처리 방식이 필요하다는 점을 확인했다.

HTTP redirect는 브라우저 화면 전환에는 자연스럽지만,
`fetch()`가 redirect를 자동으로 따라가는 환경에서는
최초 응답의 인증 실패가 최종 응답 뒤에 숨겨질 수 있다.  
따라서 API에서는 인증 실패를 `401 Unauthorized`와 `LOGIN_REQUIRED`로 명확하게 표현하고,
화면 이동과 사용자 안내는 현재 화면의 상태를 알고 있는 클라이언트가 담당하도록 역할을 분리했다.

또한 로그인 후 원래 화면으로 복귀하는 것과 중단된 변경 요청을 자동으로 재실행할지 여부는 별개의 정책임을 확인했다.  
이번 구현에서는 안전성과 구현 복잡도를 고려해 화면 복귀까지만 지원하고,
데이터 변경 요청은 사용자가 다시 실행하도록 결정했다.

후속 API 오류 처리 리팩터링에서는 HTTP 상태가 오류의 표준적인 범주를 표현하고,
애플리케이션 오류 코드는 클라이언트의 세부 동작을 결정하는 기준으로 사용할 수 있음을 확인했다.

앞으로 AJAX 기반 기능을 추가할 때는 정상 응답과 일반적인 도메인 오류뿐 아니라,
세션 만료와 같은 인증 경계 상황도 구현 및 브라우저 검증 범위에 포함한다.

---

## 2. URI 기반 공개 요청 판별로 인해 로그인 페이지로 잘못 리다이렉트되는 문제

### 상태

해결 완료

### 문제 발견 배경

게시글 목록과 게시글 상세 조회 등 비회원에게도 허용되는 공개 요청을
`LoginCheckInterceptor`에서 요청 URI 패턴으로 판별하고 있었다.

게시글 상세 조회의 공개 경로는 숫자 형태의 게시글 ID를 기준으로 판단했기 때문에
다음과 같은 정상적인 요청은 공개 요청으로 처리됐다.

```text
GET /boards/100
```

그러나 숫자로 변환할 수 없는 Path Variable이 전달되거나
Controller가 존재하지 않는 URL을 비회원이 요청하면,
Spring MVC의 오류 처리로 이어지기 전에 로그인 페이지로 리다이렉트되는 문제가 발생했다.

### 재현 환경 및 조건

로그인하지 않은 상태에서 다음 요청을 실행했다.

```text
curl -i http://localhost:8080/boards/abc
curl -i http://localhost:8080/unknown
```

두 요청 모두 로그인 페이지로의 `302 Found` 응답을 반환했다.

```text
GET /boards/abc
→ 302 Found
→ /members/login?redirectURL=%2Fboards%2Fabc

GET /unknown
→ 302 Found
→ /members/login?redirectURL=%2Funknown
```

로그인 후 `/boards/abc`로 복귀하면 그때서야 Path Variable 타입 변환에 실패하여
`400 Bad Request`가 발생했다.

### 예상 동작

두 요청은 로그인 여부와 관계없이 다음과 같이 처리되어야 한다.

```text
GET /boards/abc
→ 공개 게시글 상세 조회 Handler 선택
→ boardId의 Long 변환 실패
→ 400 Bad Request

GET /unknown
→ 정적 리소스 Handler 선택
→ 실제 리소스를 찾지 못함
→ 404 Not Found
```

그러나 기존 구현에서는 URI가 미리 정의한 공개 경로 형식과 일치하지 않으면
실제 Handler의 성격과 관계없이 보호된 SSR 요청으로 판단했다.

### 원인 분석

#### 요청 URI에 의존한 공개 접근 판별

근본 원인은 공개 접근 가능 여부를 선택된 Handler가 아닌,
요청 URI가 미리 정의한 경로나 정규식과 일치하는지를 기준으로 판별한 데 있었다.

기존 `LoginCheckInterceptor`는 요청 URI를 직접 검사하여 공개 요청 여부를 판단했다.

```java
public boolean preHandle(final HttpServletRequest request,
                         final HttpServletResponse response,
                         final Object handler) throws IOException {
    if (isPublicRequest(request)) {
        return true;
    }

    // ...
}

private static boolean isPublicRequest(final HttpServletRequest request) {
    assert (request != null);

    final String requestMethod = request.getMethod();
    final String requestURI = request.getRequestURI();

    final boolean isGet = requestMethod.equals("GET");
    final boolean isBoardList = requestURI.equals("/boards");
    final boolean isBoardDetail = requestURI.matches("^/boards/\\d+$");
    final boolean isCommentList = requestURI.matches("^/boards/\\d+/comments$");

    return isGet && (isBoardList || isBoardDetail || isCommentList);
}
```

게시글 상세 요청은 `/boards/` 뒤에 숫자가 오는 경우에만 정규식과 일치한다.

|      요청       | `isBoardDetail` | 공개 요청 판별  |
|:-------------:|:---------------:|:---------:|
| `/boards/100` |     `true`      |   공개 요청   |
| `/boards/abc` |     `false`     | 로그인 필요 요청 |
|  `/unknown`   |     `false`     | 로그인 필요 요청 |

하지만 URI의 문자열 형식이 공개 접근 가능 여부를 결정하는 것은 아니다.  
`/boards/abc`에도 공개 게시글 상세 조회 메서드가 선택되며,
Path Variable 변환 실패는 그 이후 단계에서 `400 Bad Request`로 처리되어야 한다.

#### Path Variable 변환보다 먼저 실행되는 Interceptor

`GET /boards/abc`는 `/boards/{boardId}` 매핑과 일치하므로
게시글 상세 조회 Controller 메서드를 나타내는 `HandlerMethod`가 선택된다.

Path Variable을 `Long`으로 변환하는 작업은 Interceptor를 통과한 뒤
Controller 메서드의 인자를 구성하는 과정에서 수행된다.

```text
1. HandlerMethod 선택
2. LoginCheckInterceptor.preHandle()
3. HandlerAdapter가 Controller 메서드 인자 구성
4. "abc"를 Long으로 변환하는 과정에서 실패
```

따라서 Interceptor 실행 시점에는 타입 변환 오류가 아직 발생하지 않는다.  
그런데 기존 공개 경로 정규식은 숫자 형태의 게시글 ID만 허용했기 때문에
`/boards/abc`를 공개 요청으로 판별하지 못하고 로그인 페이지로 이동시켰다.

#### 존재하지 않는 URL에도 선택되는 정적 리소스 Handler

Spring Boot는 기본적으로 정적 리소스 Handler를 `/**` 경로에 매핑한다.  
따라서 Controller 매핑이 없는 `/unknown` 요청에는 `ResourceHttpRequestHandler`가 선택된다.

그러나 이 시점에는 실제 정적 리소스의 존재 여부를 확인하지 않는다.  
리소스 탐색은 Interceptor를 통과한 뒤 Handler가 실행될 때 수행된다.

```text
1. ResourceHttpRequestHandler 선택
2. LoginCheckInterceptor.preHandle()
3. 실제 리소스 탐색
4. 리소스가 없으면 NoResourceFoundException 발생
```

기존 Interceptor는 `/unknown`을 공개 경로로 인식하지 못했기 때문에
리소스 Handler가 실행되기 전에 로그인 페이지로 리다이렉트했다.  
그 결과 `NoResourceFoundException`과 정상적인 `404 Not Found` 처리까지 도달하지 못했다.

### 해결 과정 및 설계 결정

#### Handler 메타데이터 기반 공개 요청 판별

공개 요청 여부를 URI 형식이 아닌,
선택된 Controller 메서드의 명시적인 메타데이터로 판단하도록 변경했다.

메서드 단위의 `@PublicAccess` 애너테이션을 추가하고,
비회원 접근이 허용되는 Controller 메서드에만 적용했다.

```java
@PublicAccess
@GetMapping("/{boardId}")
public String detail(...) {
    // ...
}
```

`BoardController`의 클래스 레벨 `@RequestMapping("/boards")`와 결합된
최종 요청 경로는 `GET /boards/{boardId}`이다.

`LoginCheckInterceptor`는 선택된 Handler가 `HandlerMethod`이면,
해당 메서드에 `@PublicAccess`가 있는지 확인한다.

```text
@PublicAccess 존재
→ 로그인 검사 없이 통과

@PublicAccess 없음
→ 기본적으로 로그인 필요
```

공개 여부가 Controller 메서드에 직접 표현되므로
경로 구조나 Path Variable 값의 형식에 의존하지 않는다.  
또한 새로운 요청은 별도로 공개를 선언하지 않는 한
보호되는 기본 비공개 정책을 유지한다.

#### 정적 리소스 요청 허용

선택된 Handler가 `ResourceHttpRequestHandler`인 요청은 로그인 검사 없이 통과시킨다.

```text
실제 리소스 존재
→ 정적 리소스 응답

실제 리소스 부재
→ NoResourceFoundException
→ Spring MVC 기본 오류 처리에 위임
→ 404 Not Found
```

이를 통해 CSS, JavaScript, 이미지뿐 아니라,
향후 추가되는 공개 정적 리소스도 경로별 제외 설정 없이 제공할 수 있다.  
단, 인증이 필요한 게시글 첨부 파일 등은 정적 리소스 디렉터리에 두지 않고,
별도의 Controller와 Service에서 권한 검사 이후에 제공하는 것을 원칙으로 한다.

#### Interceptor 적용 제외 경로 단순화

기존 정적 리소스 경로 및 공개 Controller 경로를 `WebConfig`의 Interceptor 적용 제외 목록에서 제거했다.  
모든 요청에 Interceptor를 적용한 뒤 `@PublicAccess`와 Handler 유형을 기준으로 통과 여부를 판단한다.  
단, 오류 디스패치 과정에서 로그인 검사가 중복되지 않도록 `/error`만 Interceptor 적용 대상에서 제외했다.

또한 알 수 없는 Handler 유형은 자동으로 공개하지 않고
기존 인증 정책을 적용하는 fail-closed 방식을 유지했다.

### 최종 해결 내용

공개 접근 여부를 요청 URI가 아닌, 선택된 Handler와 `@PublicAccess`를 기준으로 판단하도록 변경했다.  
Controller 요청은 `@PublicAccess` 적용 여부를 확인하고,
정적 리소스 요청은 `ResourceHttpRequestHandler` 여부를 확인한다.

별도로 공개가 선언되지 않은 요청은 인증이 필요한 것으로 처리하는
기본 비공개 정책을 유지한다.

|                요청 조건                 | Interceptor 결과 |                    후속 처리                     |
|:------------------------------------:|:--------------:|:--------------------------------------------:|
| `ResourceHttpRequestHandler`가 선택된 요청 |       통과       |          정적 리소스 Handler가 실제 리소스 탐색           |
| `@PublicAccess`가 적용된 `HandlerMethod` |       통과       |             공개 Controller 메서드 실행             |
|        세션에 로그인 회원 ID가 존재하는 요청        |       통과       |                선택된 Handler 실행                |
|              미인증 API 요청              |       차단       | `401 Unauthorized`와 `LOGIN_REQUIRED` JSON 응답 |
|              미인증 SSR 요청              |       차단       |                로그인 페이지로 리다이렉트                |
|   미인증 상태에서 알 수 없는 Handler가 선택된 요청    |       차단       |        로그인 페이지로 리다이렉트 (공개 요청으로 간주 X)         |

정적 리소스 요청은 Interceptor를 통과한 뒤 실제 파일의 존재 여부에 따라 결과가 결정된다.  
리소스가 존재하면 정상 응답하고, 존재하지 않으면 `NoResourceFoundException`이 발생하여
Spring MVC의 기본 오류 처리 과정을 거쳐 `404 Not Found`로 처리된다.

`WebConfig`에서는 정적 리소스와 공개 Controller 경로를 Interceptor 제외 목록에서 제거했다.  
따라서 모든 요청에 대한 공개 접근 판별은 `LoginCheckInterceptor` 내부의 동일한 기준을 사용한다.  
단, 최초 요청에서 이미 수행된 인증 검사가 오류 디스패치에서 반복되지 않도록
`/error`만 Interceptor 적용 대상에서 제외한다.

이에 따라 기존의 로그인 페이지로 잘못 이동했던 요청도
Spring MVC의 정상적인 오류 처리 흐름으로 이어진다.

```text
비회원 GET /boards/abc
→ @PublicAccess가 적용된 게시글 상세 Handler 선택
→ Interceptor 통과
→ boardId 타입 변환 실패
→ 400 Bad Request

비회원 GET /unknown
→ ResourceHttpRequestHandler 선택
→ Interceptor 통과
→ 정적 리소스 탐색 실패
→ 404 Not Found
```

### 테스트 및 재검증

`LoginCheckInterceptor` 단위 테스트를 통해 다음 동작을 검증했다.

- `@PublicAccess`가 적용된 요청은 비로그인 상태에서도 허용
- `ResourceHttpRequestHandler`가 처리하는 요청은 로그인 여부와 관계없이 허용
- 알 수 없는 Handler의 비로그인 요청은 공개로 처리하지 않고 로그인 페이지로 리다이렉트
- 기존 SSR/API 미인증 응답 분기 유지

브라우저와 `curl`을 이용한 수동 검증에서도 다음 결과를 확인했다.

- `/boards/abc`와 `/unknown`은 더 이상 로그인 페이지로 이동하지 않고
  각각 `400 Bad Request`, `404 Not Found` 오류 화면 반환
- 존재하는 정적 리소스는 로그인 여부와 관계없이 정상 응답
- 보호된 SSR 미인증 요청은 기존과 같이 로그인 페이지로 이동
- 미인증 API 요청은 기존과 같이 `401 Unauthorized`와 `LOGIN_REQUIRED` JSON 응답

### 회고

요청 URI는 어떤 Handler가 선택될지를 간접적으로 추정할 수 있을 뿐,
해당 요청의 공개 접근 정책 자체를 안정적으로 표현하지 못한다.  
경로 변수의 값이나 URL 구조가 달라지면 인증 정책까지 영향을 받는 구조에서는
새로운 요청이 추가될 때마다 정규식과 제외 경로를 함께 관리해야 한다.

이번 문제를 분석하면서 Interceptor는 Handler가 선택된 이후에 실행되지만,
Controller 인자 변환과 실제 Handler 실행보다는 먼저 동작한다는 점을 확인했다.  
따라서 Interceptor 실행 시점에는 Path Variable의 타입 변환 결과나
정적 리소스의 실제 존재 여부가 아직 결정되지 않는다.

인증 처리가 이 실행 순서를 고려하지 않으면, 잘못된 요청이 `400 Bad Request`로 처리되거나
존재하지 않는 리소스가 `404 Not Found`로 처리되어야 하는 정상적인 오류 흐름을 가로막을 수 있다.  
인증 여부를 판단하는 것과 요청 데이터 및 리소스의 유효성을 판단하는 것은
서로 다른 책임으로 분리해야 한다.

공개 접근 정책을 `@PublicAccess`로 Controller 메서드에 명시하면서
URI 구조와 인증 정책의 결합을 줄이고, 코드에서 요청의 공개 여부를 직접 확인할 수 있게 됐다.  
정적 리소스의 존재 여부와 처리 결과는 `ResourceHttpRequestHandler`에 맡김으로써
각 구성요소의 책임도 명확해졌다.

또한 공개 여부가 명확하지 않은 Handler는 자동으로 허용하지 않는 기본 비공개 정책을 유지했다.  
이를 통해 필요한 요청만 명시적으로 공개하면서도
Spring MVC의 정상적인 요청 검증과 오류 처리 과정이 실행될 수 있도록 인증 경계를 구성했다.

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

- [API Specification](./api-specification.md)
  - JSON API의 공통 요청·응답 규칙 및 오류 코드 명세
