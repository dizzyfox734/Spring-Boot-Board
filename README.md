# Spring Boot Board

Spring Boot 기반으로 구현한 게시판 웹 애플리케이션입니다.
게시글, 댓글, 회원 관리 기능을 포함하며 Spring Security 기반 인증 기능과 이메일 인증 기능을 구현했습니다.

Docker Compose를 이용하여 **Spring Boot 애플리케이션과 Redis를 컨테이너 환경에서 실행**할 수 있도록 구성했습니다.

---

# Tech Stack

### Backend

* Java
* Spring Boot
* Spring Security
* Spring Data JPA

### Database

* H2 Database (Local Development)
* MySQL (Production / AWS RDS)

### Infrastructure

* Docker
* Docker Compose
* Redis
* AWS EC2

### Template / Frontend

* Thymeleaf
* HTML / CSS / JavaScript

### Build Tool

* Gradle

---

# Main Features

### 게시판 기능

* 게시글 작성
* 게시글 목록 조회
* 게시글 상세 조회
* 게시글 수정
* 게시글 삭제

### 댓글 기능

* 댓글 작성
* 댓글 조회
* 댓글 삭제

### 회원 기능

* 회원가입
* 로그인 / 로그아웃
* 회원 정보 수정
* 아이디 찾기
* 비밀번호 찾기

### 인증 기능

* 이메일 인증

---

# Project Structure

```text
Spring-Boot-Board
├─ docker
│  ├─ docker-compose.yaml
│  ├─ nginx
│  ├─ redis
│  ├─ scripts
│  └─ web
├─ web
│  └─ spring-boot-board
│     ├─ src
│     │  ├─ main
│     │  │  ├─ java/dizzyfox734/springbootboard
│     │  │  │  ├─ common
│     │  │  │  ├─ controller
│     │  │  │  ├─ domain
│     │  │  │  ├─ exception
│     │  │  │  ├─ service
│     │  │  │  └─ util
│     │  │  └─ resources
│     │  │     ├─ templates
│     │  │     ├─ static
│     │  │     └─ yaml
│     │  └─ test
│     ├─ build.gradle
│     ├─ gradlew
│     └─ settings.gradle
├─ deploy.sh
└─ README.md
```

---

# Package Overview

### common

공통 설정 및 유틸 클래스

* `SecurityConfig`
* `JpaConfig`

### controller

웹 요청 처리

* `PostController`
* `CommentController`
* `MemberController`
* `MainController`

### domain

JPA Entity 및 Repository

* post
* comment
* member

### service

비즈니스 로직 처리

* `PostService`
* `CommentService`
* `MemberService`
* `MailService`

### exception

사용자 정의 예외 처리

---

# System Architecture

```text
Client
  │
  ▼
Spring Boot Application
  ├─ Redis
  └─ MySQL (AWS RDS / external)
```

---

# Docker Configuration

Docker Compose를 이용해 **Spring Boot 애플리케이션과 Redis를 컨테이너 환경에서 실행**할 수 있도록 구성했습니다.

현재 `docker-compose.yaml` 기준 실행 서비스

* web : Spring Boot 애플리케이션
* redis : Redis 캐시 서버

프로젝트에는 `nginx` 관련 Dockerfile 및 설정 파일도 포함되어 있으나
현재 docker-compose 파일에서는 비활성화되어 있습니다.

---

# Environment Configuration

### Local Development

* H2 Database 사용
* `application-local-db.yml`

### Mail Configuration

* `application-mail.yml`
* 이메일 인증 기능 설정

### Production

* `application-real.properties`
* AWS RDS(MySQL) 연결

---

# Run

### Application Build

```bash
cd web/spring-boot-board
./gradlew build
```

### Docker 실행

```bash
cd docker
docker-compose up -d
```

---

# Test Code

테스트 코드 포함

* `SpringBootBoardApplicationTests`
* `PostTest`
* `JpaRepositoryTest`
* `MailServiceTest`

---

# What I Learned

이 프로젝트를 통해 다음을 학습했습니다.

* Spring Boot 기반 웹 애플리케이션 구조 설계
* Spring Security를 활용한 인증 처리
* JPA Entity / Repository / Service 계층 구조 이해
* 게시판 / 댓글 / 회원 기능 구현
* 이메일 인증 및 계정 관련 기능 처리
* Docker Compose 기반 서비스 실행 환경 구성
* Redis 캐시 서버 연동
* AWS EC2 / RDS 배포 환경 구성

---

# Author

GitHub
[https://github.com/dizzyfox734](https://github.com/dizzyfox734)
