# ChatFlow (chatflow-server)

WebFlux, Redis Pub/Sub, Kafka 기반 이벤트 아키텍처 학습을 위한 **실시간 채팅** 백엔드 (포트폴리오).

**현재: 2단계** — 채팅방 · 메시지 REST

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

상세: [`docs/TECH_STACK.md`](docs/TECH_STACK.md)

| 구분 | 기술 | 버전 |
|------|------|------|
| Language | Java | 17 |
| Framework | Spring Boot | 3.2.5 |
| API | Spring WebFlux | — |
| Security | Spring Security + JWT | jjwt |
| Database | MySQL | 8.0 (Docker) |
| Data access | Spring Data R2DBC | Flyway |
| Build | Maven | pom.xml |

> 기획 엑셀: **Gradle → Maven**, **JPA → R2DBC**, **Java 17**, **Boot 3.2.5** 로 통일.

## 로드맵

1. 회원가입 · 로그인 (JWT) — 완료
2. 채팅방 · 1:1 · 그룹 · 메시지 REST ← **현재**
3. WebSocket · Redis Pub/Sub
4. 프론트 (Next.js, 별도 repo)
5. Kafka · 이벤트 기반
6. Docker
7. GitHub Actions CI/CD
8. AWS

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

## API

### 1단계 — Auth / Users (Bearer: login 후 `accessToken`)

| Method | Path |
|--------|------|
| POST | `/api/auth/signup` |
| POST | `/api/auth/login` |
| GET | `/api/users/me` |
| GET | `/api/users/{userId}` |
| GET | `/api/users/search?nickname=` |

### 2단계 — Chat (모두 Bearer)

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/chat/rooms` | 방 생성 (1:1 / 그룹) |
| GET | `/api/chat/rooms` | 내 방 목록 |
| GET | `/api/chat/rooms/{roomId}` | 방 상세 |
| POST | `/api/chat/rooms/{roomId}/messages` | 메시지 전송 |
| GET | `/api/chat/rooms/{roomId}/messages?page=0&size=50` | 메시지 목록 (최신순) |

**1:1 방 생성 Body:**
```json
{ "type": "DIRECT", "targetUserId": 2 }
```

**그룹 방 생성 Body:**
```json
{ "type": "GROUP", "roomName": "팀 채팅", "memberUserIds": [2, 3] }
```

**메시지 전송:**
```json
{ "content": "안녕하세요", "messageType": "TEXT" }
```

### 예시 (한글 nickname)

**PowerShell 5** — `-Body '...'` 문자열은 한글이 깨질 수 있음 → 스크립트 사용:

```powershell
cd C:\Users\parkj\Documents\GitHub\chatflow-server
.\scripts\api-signup.ps1 -Email "test3@test.com" -Nickname "홍길동"
```

**curl.exe** (Windows):

```bash
curl.exe -s -X POST http://localhost:8081/api/auth/signup -H "Content-Type: application/json; charset=utf-8" --data-raw "{\"email\":\"test3@test.com\",\"password\":\"password1\",\"nickname\":\"홍길동\"}"
```

**bash curl:**

```bash
curl -s -X POST http://localhost:8081/api/auth/signup \
  -H "Content-Type: application/json; charset=utf-8" \
  -d '{"email":"test3@test.com","password":"password1","nickname":"홍길동"}'

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
  auth/          # signup, login (JWT 발급)
  user/          # 프로필, 검색
  chat/          # 방·메시지 REST
  websocket/     # 3단계 예정
  common/        # 공통 예외
  config/        # Security, JWT 설정
  redis/         # 3~5단계 예정
  kafka/         # 7단계 예정
  infra/         # JWT 필터, 토큰 서비스
```

## 인증 설계 (면접용 한 줄)

Spring Security는 `permitAll`이고, **`JwtAuthenticationWebFilter`가 Bearer JWT를 검증**해 `ReactiveSecurityContextHolder`에 principal을 넣는다.

## Cursor / AI 설정

| 용도 | 경로 |
|------|------|
| User Rules 복사용 | `.cursor/user-rules.md` |
| 프로젝트 규칙 | `.cursor/rules/*.mdc` |
| 현재 단계 | `AGENTS.md` |
