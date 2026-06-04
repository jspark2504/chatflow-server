# ChatFlow (chatflow-server)

WebFlux, Redis Pub/Sub, Kafka 기반 이벤트 아키텍처 학습을 위한 **실시간 채팅** 백엔드 (포트폴리오).

**현재: 1단계** — 회원가입 · 로그인(JWT)

## 프로젝트 소개

실시간 채팅 서비스를 구현하며 WebFlux, WebSocket, Redis Pub/Sub, Kafka 등을 활용해  
이벤트 기반 아키텍처와 대규모 메시지 처리 구조를 학습하는 개인 프로젝트입니다.

## 목표

- 실시간 메시징 시스템 구현
- Reactive Programming(WebFlux) 실전 적용
- Redis 기반 메시지 브로드캐스팅 구조 설계
- Kafka 기반 이벤트 구조 확장 학습
- Docker, CI/CD, AWS 배포 경험 확보

## 기술 스택

| 구분 | 기술 |
|------|------|
| 현재 구현 | Java 17, Spring Boot 3.2, WebFlux, R2DBC, MySQL, Flyway, JWT (jjwt), BCrypt |
| 로드맵 | WebSocket, Redis Pub/Sub, Docker, GitHub Actions, AWS, Kafka |

## 로드맵

1. 회원가입 · 로그인 (JWT) ← **현재**
2. 채팅방 · 1:1 · 그룹 채팅
3. WebSocket · Redis Pub/Sub
4. Docker 배포
5. GitHub Actions CI/CD
6. AWS 배포
7. Kafka · 이벤트 기반 구조

## 로컬 실행

### 1. MySQL

```bash
docker compose up -d
```

### 2. 애플리케이션

```bash
mvn spring-boot:run
```

기본 포트: **8081** (`chat-service-server`와 8080 충돌 방지)

## API (1단계 · 기획문서 반영)

| Method | Path | 인증 |
|--------|------|------|
| POST | `/api/auth/signup` | 없음 |
| POST | `/api/auth/login` | 없음 |
| GET | `/api/users/me` | Bearer JWT |
| GET | `/api/users/{userId}` | Bearer JWT |
| GET | `/api/users/search?nickname=` | Bearer JWT |

회원 필드: `email`, `password`, `nickname` (기획문서 DB/API 명세)

### 예시

```bash
curl -s -X POST http://localhost:8081/api/auth/signup \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"test@test.com\",\"password\":\"password1\",\"nickname\":\"홍길동\"}"

curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"test@test.com\",\"password\":\"password1\"}"

curl -s http://localhost:8081/api/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## DB (기획문서 테이블정의서)

Flyway `V2__planning_schema.sql` 적용 시 생성:

- `users`, `chat_room`, `chat_room_member`, `chat_message`, `notification`, `user_connection`

2단계 이전 테이블은 DB만 준비, API는 이후 단계에서 구현.

## 패키지 구조

```
com.chatflow/
  common/error/
  user/controller/  # AuthController, UserController
  user/service/
  infra/security/
```

## 인증 설계 (면접용 한 줄)

Spring Security는 `permitAll`이고, **`JwtAuthenticationWebFilter`가 Bearer JWT를 검증**해 `ReactiveSecurityContextHolder`에 principal을 넣는다.

## Cursor / AI 설정

| 용도 | 경로 |
|------|------|
| User Rules 복사용 | `.cursor/user-rules.md` |
| 프로젝트 규칙 | `.cursor/rules/*.mdc` |
| 현재 단계 | `AGENTS.md` |
