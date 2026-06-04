# Chatflow Server — Agent 가이드

**현재 단계: 1** (회원가입 · 로그인 JWT)

## 이 저장소

실시간 채팅 백엔드 토이/포트폴리오. 상세 목표·로드맵은 `.cursor/rules/chatflow-project-context.mdc`.

## 스택

Java 17, Boot 3.2.5, WebFlux, R2DBC, Maven, MySQL, JWT — `docs/TECH_STACK.md`  
(JPA·Gradle 사용 안 함)

## 단계별 허용 기술

| 단계 | 허용 | 금지 (다음 단계까지) |
|------|------|----------------------|
| 1 | `/api/auth/signup|login`, JWT, users(email,nickname), MySQL | WebSocket, Redis, Kafka |
| 2 | 채팅방·멤버·메시지 도메인, REST | WebSocket 실시간 전송 |
| 3+ | WebSocket, Redis Pub/Sub | Kafka는 7단계 |

## 작업 시

1. `.cursor/rules/`와 멘토 규칙을 따른다.
2. 구조는 레이어드(또는 헥사고날)로, **면접에서 설명 가능**하게 유지한다.
3. 제안·PR 단위 변경에는 **이 단계에 맞는 이유**와 **면접 한 줄 설명**을 덧붙인다.
4. 전역 성향(5년차 백엔드, 공통 원칙)은 Cursor **User Rules** — 소스: `.cursor/user-rules.md`.

## 디렉터리

```
src/main/java/com/chatflow/
  auth/       controller, service, dto
  user/       controller, service, domain, repository, dto
  chat/       (2단계)
  websocket/  (3단계)
  common/error/
  config/     SecurityConfig, JwtProperties
  redis/      (3~5단계)
  kafka/      (7단계)
  infra/security/
```

## 관련 문서

- Cursor 설정 안내: `.cursor/README.md`
- User Rules 복사본: `.cursor/user-rules.md`
