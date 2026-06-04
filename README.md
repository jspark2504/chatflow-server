# chatflow-server

WebFlux, Redis Pub/Sub, Kafka 기반 이벤트 아키텍처 학습을 위한 **실시간 채팅** 백엔드 (포트폴리오).

**현재: 1단계** — 회원가입 · 로그인(JWT)

## 로드맵

1. 회원가입 · 로그인 (JWT) ← **현재**
2. 채팅방 · 1:1 · 그룹 채팅
3. WebSocket · Redis Pub/Sub
4. Docker
5. GitHub Actions CI/CD
6. AWS
7. Kafka · 이벤트 기반 구조

## 스택

Java 17 · Spring Boot 3.2 · WebFlux · R2DBC · MySQL · Flyway · JWT (jjwt) · BCrypt

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

## API (1단계)

| Method | Path | 인증 |
|--------|------|------|
| POST | `/api/users/register` | 없음 |
| POST | `/api/users/login` | 없음 |
| GET | `/api/users/me` | `Authorization: Bearer {token}` |

### 예시

```bash
# 회원가입
curl -s -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"alice\",\"password\":\"password1\"}"

# 로그인
curl -s -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"alice\",\"password\":\"password1\"}"

# 내 정보 (토큰 치환)
curl -s http://localhost:8081/api/users/me \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 패키지 구조

```
com.chatflow/
  common/error/     # 전역 예외
  user/             # 회원 API
  infra/security/   # JWT 필터, Security
```

## 인증 설계 (면접용 한 줄)

Spring Security는 `permitAll`이고, **`JwtAuthenticationWebFilter`가 Bearer JWT를 검증**해 `ReactiveSecurityContextHolder`에 principal을 넣는다.

## Cursor / AI 설정

| 용도 | 경로 |
|------|------|
| User Rules 복사용 | `.cursor/user-rules.md` |
| 프로젝트 규칙 | `.cursor/rules/*.mdc` |
| 현재 단계 | `AGENTS.md` |
