# Chatflow Server — Agent 가이드

**현재 단계: 3** (WebSocket · Redis Pub/Sub)

## 이 저장소

실시간 채팅 백엔드 토이/포트폴리오. 상세 로드맵은 `.cursor/rules/chatflow-project-context.mdc`.

## 스택

Java 17, Boot 3.2.5, WebFlux, R2DBC, Maven, MySQL, JWT, Redis — `docs/TECH_STACK.md`

## 로드맵

| 단계 | 내용 | 상태 |
|------|------|------|
| 1 | 인증·회원 | 완료 |
| 2 | 채팅 REST | 완료 |
| 3 | WebSocket · Redis | **현재** |
| 4 | 프론트 (Next.js) | 예정 (별도) |
| 5 | Kafka | 예정 |
| 6~8 | Docker · CI/CD · AWS | 예정 |

## 단계별 허용 기술

| 단계 | 허용 | 금지 |
|------|------|------|
| 1~2 | auth, users, `/api/chat/**` REST | — |
| 3 | `/ws/chat`, Redis Pub/Sub | Kafka |
| 5+ | Kafka | — |

## 디렉터리

```
com.chatflow/
  auth/
  user/
  chat/
  websocket/   handler, registry, dto
  redis/       publisher, subscriber
  infra/security/
  config/
```
