# Local Development Setup

## 개요

본 문서는 Dev Board를 로컬 환경에서 실행하고 테스트하기 위해 필요한
데이터베이스와 datasource 환경변수 설정 방법을 설명한다.

Dev Board는 테스트 실행이 개발 데이터에 영향을 주지 않도록
개발용 데이터베이스와 테스트용 데이터베이스를 분리한다.

datasource URL, 사용자 이름 및 비밀번호는 소스 코드에 직접 저장하지 않고,
애플리케이션을 실행하는 환경에서 환경변수로 전달한다.

현재는 MySQL 서버와 데이터베이스 및 테이블을 로컬 환경에 직접 준비해야 한다.

---

## 사전 준비

로컬에서 Dev Board를 실행하려면 다음 환경이 필요하다.

- Java 21
- MySQL 8.x

빌드와 테스트에는 프로젝트에 포함된 Gradle Wrapper를 사용한다.

---

## 데이터베이스 준비

Dev Board는 다음 두 데이터베이스를 구분하여 사용한다.

| 데이터베이스           | 용도                   |
|------------------|----------------------|
| `dev_board`      | 일반 애플리케이션의 개발 데이터 저장 |
| `dev_board_test` | 통합 테스트 데이터 저장        |

환경변수에 전달하는 JDBC URL을 변경하면 다른 데이터베이스 이름을 사용할 수도 있지만,
본 문서에서는 위 이름을 기준으로 설명한다.

### 데이터베이스 생성

MySQL에 다음 데이터베이스를 생성한다.

```sql
CREATE DATABASE dev_board;
CREATE DATABASE dev_board_test;
```

데이터베이스 생성에는 해당 권한을 가진 MySQL 계정이 필요하다.

### 스키마 적용

Dev Board에서 사용하는 테이블 생성 DDL은 다음 파일에서 관리한다.

```text
sql/schema.sql
```

Git Bash에서 MySQL 명령줄 클라이언트를 사용하는 경우
개발 DB와 테스트 DB에 스키마를 각각 적용한다.

```bash
mysql -u <DB_USERNAME> -p dev_board < sql/schema.sql
mysql -u <DB_USERNAME> -p dev_board_test < sql/schema.sql
```

명령을 실행하면 MySQL 계정의 비밀번호 입력을 요구한다.

`<DB_USERNAME>`에는 로컬 환경에서 사용하는 MySQL 계정명을 입력한다.  
해당 계정에는 `dev_board`와 `dev_board_test`에 접속하고 테이블을 생성할 수 있는 권한이 있어야 한다.

애플리케이션은 다음 Hibernate 설정을 사용한다.

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

따라서 애플리케이션이 테이블을 자동으로 생성하지 않는다.

실행 전에 `schema.sql`을 적용하여 필요한 테이블을 준비해야 하며,
애플리케이션 시작 시 Entity와 데이터베이스 스키마가 일치하는지 검증한다.  
단, `schema.sql`은 새로 생성한 빈 데이터베이스에 최초 스키마를 구성하기 위한 파일이다.  
이미 테이블이 생성된 데이터베이스에 다시 적용하면 테이블 중복 오류가 발생한다.

### 테스트 관리자 fixture

`schema.sql`은 테이블만 생성하며 초기 데이터를 포함하지 않는다.  
관리자 권한을 검증하는 통합 테스트에 필요한 선행 데이터는
다음 테스트 전용 SQL 파일에서 관리한다.

```text
src/test/resources/test-admin.sql
```

`IntegrationTest`는 다음 설정을 통해 각 테스트 실행 전에 관리자 fixture를 준비한다.

```java
@Sql("/test-admin.sql")
```

관리자 데이터가 없으면 테스트용 관리자를 추가하고,
이미 존재하면 테스트에 필요한 `ADMIN`, `ACTIVE` 상태를 보장한다.

fixture 데이터는 테스트 트랜잭션에 포함되어 테스트 종료 후 rollback된다.  
따라서 새로 생성한 빈 테스트 DB에 `schema.sql`을 적용한 뒤
별도의 관리자 데이터를 수동으로 등록하지 않아도 전체 통합 테스트를 실행할 수 있다.

---

## datasource 환경변수

### 개발용 환경변수

일반 애플리케이션은 다음 환경변수를 사용한다.

| 환경변수                    | 용도             | 예시                                      |
|-------------------------|----------------|-----------------------------------------|
| `DEV_BOARD_DB_URL`      | 개발 DB JDBC URL | `jdbc:mysql://localhost:3306/dev_board` |
| `DEV_BOARD_DB_USERNAME` | 개발 DB 사용자 이름   | `<DB_USERNAME>`                         |
| `DEV_BOARD_DB_PASSWORD` | 개발 DB 비밀번호     | `<DB_PASSWORD>`                         |

일반 애플리케이션의 `application.yaml`은 위 환경변수를 통해 개발 DB 접속 정보를 전달받는다.

### 테스트용 환경변수

통합 테스트와 Spring 애플리케이션 컨텍스트 테스트는
다음 환경변수를 사용한다.

| 환경변수                         | 용도              | 예시                                           |
|------------------------------|-----------------|----------------------------------------------|
| `DEV_BOARD_TEST_DB_URL`      | 테스트 DB JDBC URL | `jdbc:mysql://localhost:3306/dev_board_test` |
| `DEV_BOARD_TEST_DB_USERNAME` | 테스트 DB 사용자 이름   | `<TEST_DB_USERNAME>`                         |
| `DEV_BOARD_TEST_DB_PASSWORD` | 테스트 DB 비밀번호     | `<TEST_DB_PASSWORD>`                         |

`application-test.yaml`은 위 환경변수를 통해
테스트 DB 접속 정보를 전달받는다.

개발 DB와 테스트 DB는 동일하거나 서로 다른 MySQL 계정을 사용할 수 있다.  
두 환경의 접속 정보는 독립적으로 변경할 수 있도록 별도의 환경변수로 관리한다.

### 기본값 정책

datasource 환경변수에는 기본값을 제공하지 않는다.

필수 환경변수가 누락되면 애플리케이션 또는 통합 테스트가 시작 단계에서 실패한다.  
이를 통해 접속 정보가 없는 상태로 실행되거나 의도하지 않은 데이터베이스에 연결되는 문제를 방지한다.

---

## Spring Profile

Dev Board의 로컬 애플리케이션과 통합 테스트는 다음 프로필을 사용한다.

| 프로필     | 용도                 | 활성화 방법                             |
|---------|--------------------|------------------------------------|
| `local` | 로컬 애플리케이션 실행 환경 표시 | IntelliJ 실행 구성에서 지정                |
| `test`  | 테스트 전용 설정 적용       | 테스트 클래스의 `@ActiveProfiles("test")` |

### `local` 프로필

일반 애플리케이션은 IntelliJ의 `DevBoardApplication` 실행 구성에서 `local` 프로필을 활성화한다.

현재는 별도의 `application-local.yaml`을 사용하지 않으며,
공통 설정 파일인 `application.yaml`을 그대로 사용한다.  
`local` 프로필은 현재 실행 환경이 로컬 개발 환경임을 명시하기 위해 사용한다.  
프로필은 코드나 설정 파일에 고정하지 않고 애플리케이션 실행 환경에서 지정한다.

### `test` 프로필

데이터베이스를 사용하는 통합 테스트와 애플리케이션 컨텍스트 테스트는
다음 설정을 통해 `test` 프로필을 활성화한다.

```java
@ActiveProfiles("test")
```

이에 따라 `src/test/resources/application-test.yaml`의 datasource 설정이 적용되어
개발 DB가 아닌 테스트 DB에 연결된다.

---

## IntelliJ 애플리케이션 실행 설정

다음 경로에서 `DevBoardApplication` 실행 구성을 연다.

```text
Run → Edit Configurations → Spring Boot → DevBoardApplication
```

`Active profiles`에는 다음 값을 입력한다.

```text
local
```

`Environment variables`에는 다음 환경변수를 등록한다.

```text
DEV_BOARD_DB_URL=jdbc:mysql://localhost:3306/dev_board
DEV_BOARD_DB_USERNAME=<DB_USERNAME>
DEV_BOARD_DB_PASSWORD=<DB_PASSWORD>
```

`<DB_USERNAME>`과 `<DB_PASSWORD>`에는 로컬 MySQL 환경에 맞는 접속 정보를 입력한다.

애플리케이션 실행 후 로그에서 다음 사항을 확인한다.

- `local` 프로필 활성화
- `dev_board` 데이터베이스 연결
- 애플리케이션 시작 성공

애플리케이션이 실행되면 다음 주소에 접근할 수 있다.

```text
http://localhost:8080
```

---

## IntelliJ 테스트 설정

개별 테스트마다 테스트용 환경변수를 반복해서 등록하지 않도록
IntelliJ의 Gradle 실행 구성 템플릿에 환경변수를 등록한다.

다음 경로에서 Gradle 템플릿을 연다.

```text
Run → Edit Configurations → Edit configuration templates → Gradle
```

`Environment variables`에는 다음 값을 등록한다.

```text
DEV_BOARD_TEST_DB_URL=jdbc:mysql://localhost:3306/dev_board_test
DEV_BOARD_TEST_DB_USERNAME=<TEST_DB_USERNAME>
DEV_BOARD_TEST_DB_PASSWORD=<TEST_DB_PASSWORD>
```

통합 테스트 실행 후 로그에서 다음 사항을 확인한다.

- `test` 프로필 활성화
- `dev_board_test` 데이터베이스 연결
- 테스트 성공

---

## Git Bash 테스트 실행

Git Bash에서 전체 테스트를 실행하려면 현재 셸에 테스트용 환경변수를 설정해야 한다.

URL과 사용자 이름을 입력한다.

```bash
read -r -p "Test DB URL: " DEV_BOARD_TEST_DB_URL
read -r -p "Test DB username: " DEV_BOARD_TEST_DB_USERNAME
```

비밀번호는 입력값이 화면에 표시되지 않도록 `-s` 옵션을 사용한다.

```bash
read -r -s -p "Test DB password: " DEV_BOARD_TEST_DB_PASSWORD
printf '\n'
```

입력한 셸 변수를 Gradle 프로세스에 전달할 수 있도록 내보낸다.

```bash
export DEV_BOARD_TEST_DB_URL
export DEV_BOARD_TEST_DB_USERNAME
export DEV_BOARD_TEST_DB_PASSWORD
```

전체 테스트를 실행한다.

```bash
./gradlew clean test
```

현재 셸에 설정한 환경변수는 같은 Git Bash 세션에서 다시 사용할 수 있다.  
Git Bash 창을 종료하면 해당 셸에 설정한 값도 사라진다.

---

## 보안 주의사항

- 실제 DB 접속 정보는 소스 코드, 문서 및 Git 커밋에 포함하지 않는다.
- 비밀번호는 터미널 명령어에 직접 작성하지 않고 입력 프롬프트나 실행 환경의 환경변수 설정을 사용한다.
- 접속 정보가 외부에 노출되면 저장소에서 문자열만 제거하는 것으로 끝내지 않고 실제 DB 비밀번호를 변경한다.

IntelliJ에 등록한 환경변수는 개인 로컬 실행 설정이며 Git 저장소를 통해 공유되지 않는다.  
각 실행자는 자신의 로컬 환경에 맞는 값을 직접 설정해야 한다.

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
  - 주요 아키텍처 설계 의사결정 기록

- [Project Progress](./project-progress.md)
  - 현재 프로젝트 진행 현황 및 개발 계획

- [Troubleshooting](./troubleshooting.md)
  - 개발 과정에서 발생한 주요 문제의 원인 분석 및 해결 과정 기록
