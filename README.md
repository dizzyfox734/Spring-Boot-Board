# Spring Boot Board

Spring Boot 기반 게시판 웹 애플리케이션입니다. 게시글, 댓글, 회원 기능을 단순 구현하는 데서 끝내지 않고, 인증/인가, 이메일 인증, Redis 기반 인증코드 저장, 예외 처리, 테스트, Docker 배포 구성을 함께 다룬 포트폴리오 프로젝트입니다.

## 프로젝트 목표

- Spring MVC, Thymeleaf, Spring Data JPA를 사용한 서버 사이드 렌더링 게시판 구현
- Spring Security 기반 로그인/로그아웃, 권한별 접근 제어, 비밀번호 암호화 적용
- 회원가입 이메일 인증코드를 Redis에 TTL 기반으로 저장하고 검증
- 게시글/댓글 작성자 검증, 입력값 검증, 도메인 예외 처리를 서비스 계층에 반영
- 단위 테스트, MVC 테스트, JPA 통합 테스트로 주요 비즈니스 규칙 검증
- Docker Compose와 배포 스크립트를 통해 EC2 환경에서 실행 가능한 형태로 구성

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Language | Java 17 |
| Backend | Spring Boot 3.1.0, Spring MVC, Spring Security, Spring Data JPA, Validation |
| View | Thymeleaf, Thymeleaf Layout Dialect, HTML, CSS, JavaScript |
| Database | H2(local), MySQL/AWS RDS(production configuration) |
| Cache | Redis |
| Mail | Spring Mail, SMTP |
| Test | JUnit 5, Spring Boot Test, Spring Security Test, Mockito |
| Infra | Docker, Docker Compose, AWS EC2 |
| Build | Gradle |

## 주요 기능

### 게시판

- 게시글 목록, 상세 조회
- 게시글 작성, 수정, 삭제
- 제목, 본문, 작성자, 댓글 작성자 기준 검색
- 작성자 본인만 수정/삭제 가능한 권한 검증
- Markdown 렌더링 유틸 적용

### 댓글

- 게시글별 댓글 작성 및 조회
- 댓글 삭제
- 로그인 사용자 기준 작성자 검증

### 회원

- 회원가입, 로그인, 로그아웃
- 회원 정보 조회 및 비밀번호 변경
- 아이디 찾기
- 비밀번호 찾기 및 임시 비밀번호 메일 발송
- BCrypt 기반 비밀번호 암호화

### 이메일 인증

- 회원가입 이메일 인증코드 발송
- Redis에 인증코드 저장 및 만료 시간 관리
- 인증코드 검증 성공 시 Redis 데이터 삭제
- 메일 발송 실패 시 기존 인증코드 복구 처리

## 아키텍처

```text
Client
  |
  v
Spring Boot Application
  |-- Spring MVC / Thymeleaf
  |-- Spring Security
  |-- Service Layer
  |-- Spring Data JPA ---- H2(local) / MySQL(AWS RDS)
  |
  `-- Redis
      `-- Email certification code with TTL
```

## 패키지 구조

```text
web/spring-boot-board/src/main/java/dizzyfox734/springbootboard
├─ post
│  ├─ controller
│  ├─ domain
│  ├─ repository
│  └─ service
├─ comment
│  ├─ controller
│  ├─ domain
│  ├─ repository
│  └─ service
├─ member
│  ├─ controller
│  ├─ domain
│  ├─ exception
│  ├─ repository
│  └─ service
├─ mail
│  ├─ domain
│  ├─ exception
│  ├─ repository
│  └─ service
├─ global
│  ├─ config
│  ├─ exception
│  ├─ utils
│  └─ validation
└─ common
   └─ entity
```

## 핵심 구현 포인트

### 인증과 권한 관리

- `SecurityConfig`에서 공개 페이지, 인증 필요 페이지, 정적 리소스, H2 콘솔 접근 정책을 분리했습니다.
- 게시글/댓글 수정과 삭제는 서비스 계층에서 작성자 검증을 수행해 컨트롤러 외부에서도 동일한 규칙이 적용되도록 했습니다.
- 회원 비밀번호는 `BCryptPasswordEncoder`로 암호화해 저장합니다.

### 이메일 인증 흐름

- `MailCertificationService`가 인증코드 생성, Redis 저장, SMTP 발송, 검증을 담당합니다.
- 인증코드는 Redis에 TTL과 함께 저장되며, 만료되었거나 값이 일치하지 않으면 별도 예외를 발생시킵니다.
- 재발송 중 메일 발송이 실패하면 새 인증코드 저장으로 인해 기존 인증 상태가 깨지지 않도록 이전 인증코드를 복구합니다.

### 게시글 검색

- `PostService`에서 JPA `Specification`을 사용해 제목, 본문, 게시글 작성자, 댓글 작성자 기준 검색 조건을 구성했습니다.
- 페이징은 `PageRequest`와 생성일 내림차순 정렬을 적용했습니다.

### 예외 처리와 검증

- `DataNotFoundException`, `AccessDeniedException`, `InvalidRequestException` 등 도메인 상황에 맞는 예외를 분리했습니다.
- `GlobalViewExceptionHandler`에서 화면 요청에 대한 공통 예외 응답을 처리합니다.
- 비밀번호 확인 검증은 커스텀 validation annotation으로 분리했습니다.

## 테스트

테스트는 계층별로 분리되어 있습니다.

- Domain test: `PostTest`, `CommentTest`, `MemberTest`
- Service test: `PostServiceTest`, `CommentServiceTest`, `MemberServiceTest`, `MailServiceTest`, `MailCertificationServiceTest`
- Controller test: `PostControllerTest`, `CommentControllerTest`, `MemberControllerTest`
- Repository integration test: `PostRepositoryJpaIntegrationTest`, `CommentRepositoryJpaIntegrationTest`, `MemberRepositoryJpaIntegrationTest`
- Exception handler test: `GlobalViewExceptionHandlerTest`

```bash
cd web/spring-boot-board
./gradlew test
```

## 실행 방법

### 로컬 실행

로컬 기본 프로필은 H2 DB와 메일 설정을 사용합니다. 메일 인증 기능을 사용하려면 SMTP 계정 정보를 환경 변수로 설정해야 합니다.

```bash
export SPRING_MAIL_USERNAME=your-email@example.com
export SPRING_MAIL_PASSWORD=your-app-password

cd web/spring-boot-board
./gradlew bootRun
```

빌드만 수행할 때는 다음 명령을 사용합니다.

```bash
cd web/spring-boot-board
./gradlew build
```

### Docker Compose 실행

`docker/env`에 포트, 프로젝트 이름, 메일 계정 정보를 설정한 뒤 실행합니다.

```bash
cd docker
docker compose --env-file env up -d --build
```

현재 Compose 구성의 활성 서비스는 다음과 같습니다.

- `web`: Spring Boot 애플리케이션
- `redis`: 이메일 인증코드 저장용 Redis

`nginx` Dockerfile과 설정은 포함되어 있지만 현재 `docker-compose.yaml`에서는 비활성화되어 있습니다.

## 설정 파일

| 파일 | 설명 |
| --- | --- |
| `application.yml` | 기본 프로필 및 공통 설정 |
| `yaml/application-local-db.yml` | 로컬 H2 DB 설정 |
| `yaml/application-mail.yml` | SMTP 및 인증코드 설정 |
| `yaml/application-real.properties` | 운영 DB 설정 예시 |
| `docker/env` | Docker Compose 환경 변수 |

## 배포

`deploy.sh`는 EC2 서버의 지정된 경로에서 기존 컨테이너를 종료하고, 이전 JAR를 백업한 뒤 Gradle 빌드와 Docker Compose 실행 스크립트를 호출하는 흐름으로 구성되어 있습니다.

```text
destroy container -> backup previous jar -> gradle build -> create container
```

## 회고

이 프로젝트를 통해 게시판 CRUD뿐 아니라 인증/인가, 이메일 인증, Redis TTL 저장소, 예외 처리, 테스트 전략, Docker 기반 실행 환경까지 하나의 애플리케이션 흐름으로 연결하는 경험을 정리했습니다. 특히 서비스 계층에 비즈니스 규칙을 두고 테스트로 검증하는 구조를 만들면서, 기능 구현보다 유지보수 가능한 경계 설정이 더 중요하다는 점을 학습했습니다.

## 작성자

- GitHub: [dizzyfox734](https://github.com/dizzyfox734)
