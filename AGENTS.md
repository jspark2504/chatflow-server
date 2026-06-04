# Chatflow Server — Agent 가이드

**현재 단계: 5** (Kafka 이벤트)

## 이 저장소

실시간 채팅 백엔드 토이/포트폴리오. 상세 로드맵은 `.cursor/rules/chatflow-project-context.mdc`.

## 스택

Java 17, Boot 3.2.5, WebFlux, R2DBC, Maven, MySQL, JWT, Redis, Kafka — `docs/TECH_STACK.md`

## 로드맵

| 단계 | 내용 | 상태 |
|------|------|------|
| 1 | 인증·회원 | 완료 |
| 2 | 채팅 REST | 완료 |
| 3 | WebSocket · Redis | 완료 |
| 4 | 프론트 (Next.js) | 예정 (별도) |
| 5 | Kafka | **현재** |
| 6~8 | Docker · CI/CD · AWS | 예정 |

## 메시지 파이프라인

저장 후: `Redis` (실시간 WS) + `Kafka` (`chat.message.sent`)

## 디렉터리

```
com.chatflow/
  kafka/       publisher, consumer, dto
  redis/
  websocket/
  chat/
```
