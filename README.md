# Spring Boot Board

Spring Boot 기반으로 구현한 게시판 서비스입니다.
게시글 및 댓글 기능과 회원 관리 기능을 포함하고 있으며 Docker 환경을 통해 배포할 수 있도록 구성했습니다.

---

# Tech Stack

### Backend

* Java
* Spring Boot
* Spring Security
* Spring Data JPA

### Database

* MySQL (AWS RDS)
* H2 Database (Local Development)

### Infrastructure

* Docker
* Nginx
* Redis
* AWS EC2

### Build Tool

* Gradle

---

# Main Features

### 게시판 기능

* 게시글 작성
* 게시글 조회
* 게시글 수정
* 게시글 삭제

### 댓글 기능

* 댓글 작성
* 댓글 조회
* 댓글 삭제

### 회원 기능

* 회원 가입
* 로그인
* 회원 정보 수정
* 아이디 / 비밀번호 찾기

### 인증 기능

* 이메일 인증 기능

---

# System Architecture

```
Client
   │
   ▼
Nginx
   │
   ▼
Spring Boot Application
   │
   ├── Redis
   │
   └── MySQL (AWS RDS)
```

---

# Project Structure

```
spring-boot-board
 ├─ controller
 │   ├─ CommentController
 │   └─ ...
 │
 ├─ member
 ├─ post
 ├─ comment
 │
 ├─ common
 │   └─ config
 │       ├─ SecurityConfig
 │       └─ JpaConfig
 │
 ├─ resources
 │   ├─ application.yml
 │   └─ yaml
 │
 └─ build.gradle
```

---

# Docker Environment

프로젝트는 Docker 환경에서 실행할 수 있도록 구성되어 있습니다.

구성

```
docker
 ├─ nginx
 ├─ redis
 ├─ web
 └─ docker-compose.yaml
```

서비스 실행

```bash
docker-compose up -d
```

---

# Development Environment

Local 환경

```
Spring Boot
H2 Database
```

Production 환경

```
Spring Boot
AWS EC2
MySQL (AWS RDS)
Docker
```

---

# What I Learned

이 프로젝트를 통해 다음을 학습했습니다.

* Spring Boot 기반 웹 애플리케이션 구조
* Spring Security 인증 처리
* JPA 기반 데이터 관리
* Docker 기반 서비스 실행 환경 구성
* AWS EC2 / RDS 배포 환경 구성

---

# Author

GitHub
[https://github.com/dizzyfox734](https://github.com/dizzyfox734)

---
