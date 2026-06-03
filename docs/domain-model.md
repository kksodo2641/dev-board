# Domain Model

## 개요

본 문서는 dev-board 프로젝트의 **핵심 도메인과 비즈니스 규칙을 정의**하기 위한 문서이다.

ERD, API 명세, 구현 상세는 별도 문서에서 관리한다.

## 설계 원칙

- 객체 중심으로 도메인을 모델링한다.
- 비즈니스 규칙은 가능한 한 도메인 중심으로 관리한다.
- 삭제 정책은 일관성을 위해 Soft Delete를 우선 적용한다.
- 권한 정책(USER, ADMIN)을 명확히 분리한다.

## 공통 엔티티 정책

모든 주요 엔티티(Member, Board, Comment, BoardLike, UploadFile)는 생성일시와 수정일시를 관리한다.

- createdAt: 생성일시
- updatedAt: 수정일시

## 도메인 관계 상세

### 1. Member ↔ Board

- 하나의 Member는 여러 개의 Board를 작성할 수 있다.
- 하나의 Board는 반드시 하나의 Member에 의해서만 작성된다.

Cardinality: One-to-Many(1:N)

### 2. Member ↔ Comment

- 하나의 Member는 여러 개의 Comment를 작성할 수 있다.
- 하나의 Comment는 반드시 하나의 Member에 의해서만 작성된다.

Cardinality: One-to-Many(1:N)

### 3. Board ↔ Comment

- 하나의 Board는 여러 개의 Comment를 가질 수 있다.
- 하나의 Comment는 반드시 하나의 Board에만 속한다.

Cardinality: One-to-Many(1:N)

### 4. Comment ↔ Comment

- 하나의 Comment는 여러 개의 대댓글을 가질 수 있다.
- 하나의 대댓글은 하나의 부모 Comment를 가진다.
- 최상위 댓글은 부모 Comment를 가지지 않는다.
- 댓글과 대댓글은 동일한 Comment 도메인으로 관리한다.

Cardinality: One-to-Many(1:N, Self Reference)

### 5. Board ↔ UploadFile

- 하나의 Board는 여러 개의 UploadFile을 가질 수 있다.
- 하나의 UploadFile은 반드시 하나의 Board에 속한다.

Cardinality: One-to-Many(1:N)

### 6. Member ↔ Board (좋아요 관점)

- 하나의 Member는 여러 개의 Board에 좋아요를 누를 수 있다.
- 하나의 Board는 여러 Member로부터 좋아요를 받을 수 있다.
- 이같은 다대다 관계는 중간 테이블 BoardLike를 통해 해소한다.

Cardinality: Many-to-Many(M:N)

### 7. Member ↔ BoardLike

- 하나의 Member는 여러 개의 BoardLike를 가질 수 있다.
- 하나의 BoardLike는 반드시 하나의 Member에 의해 생성된다.
- 동일 회원은 동일 게시글에 대해 하나의 BoardLike만 가질 수 있다.

Cardinality: One-to-Many(1:N)

### 8. Board ↔ BoardLike

- 하나의 Board는 여러 개의 좋아요를 가질 수 있다.
- 하나의 BoardLike는 반드시 하나의 Board를 대상으로 한다.

Cardinality: One-to-Many(1:N)

## 핵심 도메인

### 1. Member

#### 역할

- **회원**을 나타내는 도메인

#### 주요 책임

- 회원가입
- 회원탈퇴
- 로그인
- 로그아웃
- 게시글 작성
- 댓글 작성
- 좋아요 등록

#### 비즈니스 규칙

- 이메일(email)을 로그인 ID로 사용한다.
- 이메일은 중복될 수 없다.
- 닉네임은 중복될 수 없다.
- 비밀번호는 BCrypt 해시로 저장한다.
- 회원 탈퇴 시 소프트 삭제를 적용한다.
- 회원은 스스로 탈퇴할 수 있다.
- 탈퇴한 회원은 로그인할 수 없다.
- 탈퇴한 회원의 작성 게시글 및 댓글은 유지한다.

### 2. Board

#### 역할

- **게시글**을 나타내는 도메인

#### 주요 책임

- 게시글 작성
- 게시글 조회
- 게시글 수정
- 게시글 삭제

#### 비즈니스 규칙

- 게시글 작성자만 수정할 수 있다.
- 게시글 작성자만 삭제할 수 있다.
- 관리자는 모든 게시글을 삭제할 수 있다.
- 게시글 삭제 시 소프트 삭제를 적용한다.
- 삭제된 게시글은 "삭제된 게시글입니다." 형태로 표시한다.
- 게시글 조회 시 조회수를 증가시킨다.
- 게시글은 하나의 카테고리를 가진다.
- 게시글은 여러 개의 첨부파일을 가질 수 있다.

### 3. Comment

#### 역할

- **댓글 및 대댓글**을 나타내는 도메인

#### 주요 책임

- 댓글 작성
- 댓글 수정
- 댓글 삭제
- 대댓글 작성

#### 비즈니스 규칙

- 댓글 작성자만 수정할 수 있다.
- 댓글 작성자만 삭제할 수 있다.
- 관리자는 모든 댓글을 삭제할 수 있다.
- 댓글 삭제 시 소프트 삭제를 적용한다.
- 삭제된 댓글은 "삭제된 댓글입니다." 형태로 표시한다.
- 삭제된 댓글의 대댓글은 유지한다.
- 댓글은 부모 댓글을 가질 수 있다. 이를 통해 대댓글 기능을 지원한다.

### 4. BoardLike

#### 역할

- 게시글 **좋아요**를 나타내는 도메인

#### 주요 책임

- 좋아요 등록
- 좋아요 취소

#### 비즈니스 규칙

- 동일 회원은 동일 게시글에 한 번만 좋아요를 누를 수 있다.
- 좋아요 취소가 가능하다.
- 좋아요 중복 등록을 방지한다.

### 5. UploadFile

#### 역할

- 게시글 **첨부파일**을 나타내는 도메인

#### 주요 책임

- 파일 업로드
- 파일 조회

#### 비즈니스 규칙

- 게시글당 여러 개의 파일을 업로드할 수 있다.
- 게시글 삭제 시 파일은 유지한다.

## 권한 정책

### 1. USER

- 본인 게시글 수정 가능
- 본인 게시글 삭제 가능
- 본인 댓글 수정 가능
- 본인 댓글 삭제 가능

### 2. ADMIN

- 모든 게시글 삭제 가능
- 모든 댓글 삭제 가능
- 회원 상태(ACTIVE / DELETED) 관리 가능

## 삭제 정책

도메인 간 참조 무결성 및 이력 보존을 위해, 주요 도메인(Member, Board, Comment)은 Soft Delete를 적용한다.
- Hard Delete: 실제 DB에서 삭제 수행
- Soft Delete: 실제 DB에서 삭제 수행 X

| 도메인        | 삭제 방식       |
|------------|-------------|
| Member     | Soft Delete |
| Board      | Soft Delete |
| Comment    | Soft Delete |
| BoardLike  | Hard Delete |
| UploadFile | 삭제하지 않음     |

## Enum

### 1. Role

- USER
- ADMIN

### 2. MemberStatus

- ACTIVE
- DELETED

### 3. BoardStatus

- ACTIVE
- DELETED

### 4. CommentStatus

- ACTIVE
- DELETED

### 5. BoardCategory

- NOTICE (공지사항)
- FREE (자유 게시판)
- QNA (질문 게시판)
- STUDY (스터디 게시판)
- JOB (취업 게시판)

### 6. Gender
- MALE
- FEMALE
- NONE

## 인증 및 인가 정책

- 이메일 기반 로그인 사용
- 비밀번호는 BCrypt 해시 저장
- 세션 기반 로그인 사용 (향후 토큰 방식 교체 고려)
    - 로그인 성공 시 서버 세션 생성
- USER / ADMIN 권한 분리

---

## 향후 확장 고려 사항

- 게시글 신고 기능
- 관리자 페이지
- 게시글 북마크 기능
- 알림 기능
