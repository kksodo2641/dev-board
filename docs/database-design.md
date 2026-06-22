# Database Design

## 개요

본 문서는 Dev Board 프로젝트의 데이터베이스 관점의 설계 문서이다.

각 테이블의 컬럼 및 제약조건을 정의하며, 테이블 간 관계를 명시한다.

도메인 규칙 및 비즈니스 정책은 [Domain Design](./domain-design.md) 문서를 따르며,

주요 아키텍처 설계 의사결정은 [Architecture Decisions](./architecture-decisions.md) 문서를 따른다.

---

## 핵심 엔티티

### 1. MEMBER

#### 설명

**회원** 정보를 저장하는 테이블

Soft Delete 정책을 적용하며, 탈퇴 회원 정보는 물리 삭제하지 않는다.

#### 컬럼 및 제약조건

| 컬럼명           | 타입           | 제약조건                                                                             | 설명               |
|---------------|--------------|----------------------------------------------------------------------------------|------------------|
| member_id     | BIGINT       | PK (auto_increment)                                                              | 회원 ID            |
| email         | VARCHAR(255) | NOT NULL, UNIQUE                                                                 | 이메일, 로그인 ID로 사용  |
| password_hash | VARCHAR(255) | NOT NULL                                                                         | 비밀번호, BCrypt 해시값 |
| nickname      | VARCHAR(30)  | NOT NULL, UNIQUE CHECK (char_length(nickname) between 2 and 30)                  | 닉네임              |
| gender        | VARCHAR(10)  | NOT NULL, <br/> CHECK(gender IN ('MALE', 'FEMALE', 'NONE')),<br/> DEFAULT 'NONE' | 성별               |
| role          | VARCHAR(10)  | NOT NULL, <br/> CHECK(role IN ('USER', 'ADMIN')),<br/> DEFAULT 'USER'            | 권한               |
| status        | VARCHAR(20)  | NOT NULL, <br/> CHECK(status IN ('ACTIVE', 'DELETED')),<br/> DEFAULT 'ACTIVE'    | 상태               |
| created_at    | DATETIME     | NOT NULL                                                                         | 생성 일시            |
| updated_at    | DATETIME     | NOT NULL                                                                         | 수정 일시            |

### 2. BOARD

#### 설명

**게시글** 정보를 저장하는 테이블

Soft Delete 정책을 적용하며, 삭제된 게시글도 데이터는 유지된다.

#### 컬럼 및 제약조건

| 컬럼명        | 타입           | 제약조건                                                                          | 설명        |
|------------|--------------|-------------------------------------------------------------------------------|-----------|
| board_id   | BIGINT       | PK (auto_increment)                                                           | 게시글 ID    |
| member_id  | BIGINT       | FK, NOT NULL                                                                  | 작성자 회원 ID |
| title      | VARCHAR(100) | NOT NULL                                                                      | 제목        |
| content    | TEXT         | NOT NULL                                                                      | 본문        |
| category   | VARCHAR(20)  | NOT NULL,<br/> CHECK(category IN ('NOTICE', 'FREE', 'QNA', 'STUDY', 'JOB'))   | 게시판 카테고리  |
| status     | VARCHAR(20)  | NOT NULL, <br/> CHECK(status IN ('ACTIVE', 'DELETED')),<br/> DEFAULT 'ACTIVE' | 상태        |
| view_count | INT          | NOT NULL, DEFAULT 0                                                           | 조회수       |
| created_at | DATETIME     | NOT NULL                                                                      | 생성 일시     |
| updated_at | DATETIME     | NOT NULL                                                                      | 수정 일시     |

### 3. COMMENT

#### 설명

**댓글 및 대댓글** 정보를 저장하는 테이블

(향후 구현 예정)

#### 컬럼 및 제약조건

| 컬럼명        | 타입          | 제약조건                                                                          | 설명        |
|------------|-------------|-------------------------------------------------------------------------------|-----------|
| comment_id | BIGINT      | PK (auto_increment)                                                           | 댓글 ID     |
| board_id   | BIGINT      | FK, NOT NULL                                                                  | 게시글 ID    |
| member_id  | BIGINT      | FK, NOT NULL                                                                  | 작성자 회원 ID |
| parent_id  | BIGINT      | FK, NULL                                                                      | 부모 댓글 ID  |
| content    | TEXT        | NOT NULL                                                                      | 댓글 내용     |
| status     | VARCHAR(20) | NOT NULL, <br/> CHECK(status IN ('ACTIVE', 'DELETED')),<br/> DEFAULT 'ACTIVE' | 상태        |
| created_at | DATETIME    | NOT NULL                                                                      | 생성 일시     |
| updated_at | DATETIME    | NOT NULL                                                                      | 수정 일시     |

#### 비고

- parent_id가 NULL이면 최상위 댓글을 의미
- parent_id가 존재하면 대댓글을 의미

### 4. BOARD_LIKE

#### 설명

**좋아요** 정보를 저장하는 테이블

(향후 구현 예정)

#### 컬럼 및 제약조건

| 컬럼명           | 타입       | 제약조건                | 설명     |
|---------------|----------|---------------------|--------|
| board_like_id | BIGINT   | PK (auto_increment) | 좋아요 ID |
| board_id      | BIGINT   | FK, NOT NULL        | 게시글 ID |
| member_id     | BIGINT   | FK, NOT NULL        | 회원 ID  |
| created_at    | DATETIME | NOT NULL            | 생성 일시  |
| updated_at    | DATETIME | NOT NULL            | 수정 일시  |

#### Unique Constraint

- (board_id, member_id)

#### 비고

- 동일 회원은 동일 게시글에 딱 한 번만 좋아요를 누를 수 있다.

### 5. UPLOAD_FILE

#### 설명

**첨부파일** 정보를 저장하는 테이블

#### 컬럼 및 제약조건

| 컬럼명                | 타입           | 제약조건                | 설명           |
|--------------------|--------------|---------------------|--------------|
| upload_file_id     | BIGINT       | PK (auto_increment) | 업로드 파일 ID    |
| board_id           | BIGINT       | FK, NOT NULL        | 게시글 ID       |
| original_file_name | VARCHAR(255) | NOT NULL            | 사용자 업로드 파일명  |
| stored_file_name   | VARCHAR(255) | NOT NULL            | 서버 저장 파일명    |
| file_size          | BIGINT       | NOT NULL            | 파일 크기 (byte) |
| created_at         | DATETIME     | NOT NULL            | 생성 일시        |
| updated_at         | DATETIME     | NOT NULL            | 수정 일시        |

---

## 테이블 관계

### 1. MEMBER ↔ BOARD

#### Cardinality

- MEMBER (1) : BOARD (N)

#### 외래키 제약조건

- BOARD.member_id (FK) → MEMBER.member_id (PK)

#### 설명

- 하나의 회원은 여러 개의 게시글을 작성할 수 있다.
- 하나의 게시글은 반드시 하나의 회원에 의해 작성된다.

### 2. MEMBER ↔ COMMENT

#### Cardinality

- MEMBER (1) : COMMENT (N)

#### 외래키 제약조건

- COMMENT.member_id (FK) → MEMBER.member_id (PK)

#### 설명

- 하나의 회원은 여러 개의 댓글을 작성할 수 있다.
- 하나의 댓글은 반드시 하나의 회원에 의해 작성된다.

### 3. BOARD ↔ COMMENT

#### Cardinality

- BOARD (1) : COMMENT (N)

#### 외래키 제약조건

- COMMENT.board_id (FK) → BOARD.board_id (PK)

#### 설명

- 하나의 게시글은 여러 개의 댓글을 가질 수 있다.
- 하나의 댓글은 반드시 하나의 게시글에 속한다.

### 4. COMMENT ↔ COMMENT

#### Cardinality

- COMMENT (1) : COMMENT (N)

#### 외래키 제약조건

- COMMENT.parent_id (FK) → COMMENT.comment_id (PK) (Self Reference FK)

#### 설명

- 하나의 댓글은 여러 개의 대댓글을 가질 수 있다.
- 하나의 대댓글은 반드시 하나의 부모 댓글에 속한다.
- 최상위 댓글은 parent_id가 NULL이다.

### 5. BOARD ↔ UPLOAD_FILE

#### Cardinality

- BOARD (1) : UPLOAD_FILE (N)

#### 외래키 제약조건

- UPLOAD_FILE.board_id (FK) → BOARD.board_id (PK)

#### 설명

- 하나의 게시글은 여러 개의 첨부파일을 가질 수 있다.
- 하나의 첨부파일은 반드시 하나의 게시글에 속한다.

### 6. MEMBER ↔ BOARD_LIKE

#### Cardinality

- MEMBER (1) : BOARD_LIKE (N)

#### 외래키 제약조건

- BOARD_LIKE.member_id (FK) → MEMBER.member_id (PK)

#### 설명

- 하나의 회원은 여러 개의 게시글에 좋아요를 누를 수 있다.
- 하나의 좋아요는 반드시 하나의 회원에 속한다.

### 7. BOARD ↔ BOARD_LIKE

#### Cardinality

- BOARD (1) : BOARD_LIKE (N)

#### 외래키 제약조건

- BOARD_LIKE.board_id (FK) → BOARD.board_id (PK)

#### 설명

- 하나의 게시글은 여러 개의 좋아요를 가질 수 있다.
- 하나의 좋아요는 반드시 하나의 게시글에 속한다.

--- 

## ERD Diagram

본 프로젝트의 데이터베이스 구조를 시각적으로 표현한 ERD이다.

![ERD](./images/erd.png)

---

## 프로젝트 문서

- [Domain Design](./domain-design.md)
  - 도메인 모델 및 비즈니스 규칙 정의

- [Architecture Decisions](./architecture-decisions.md)
  - 주요 아키텍처 설계 의사결정 정의

- [Project Progress](./project-progress.md)
  - 현재 프로젝트 진행 현황 및 개발 계획


