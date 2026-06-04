# Postman — Chatflow Server (테스트용)

## 가져오기

| 용도 | 파일 |
|------|------|
| REST (로그인·채팅) | `Chatflow-Server.postman_collection.json` + `local.postman_environment.json` |
| **WebSocket** | **`postman/ws-test.html`** (권장) · [README-WebSocket.md](README-WebSocket.md) · Postman은 **New → WebSocket** 수동 |

1. Postman **Import** → 위 파일 선택
2. 환경 선택 (우츹 상단)

> `localhost` / WebSocket: **Postman 데스크톱** + Desktop Agent. 웹만 쓰면 WS·로컬 호출이 실패할 수 있습니다.

## 사전 조건

```bash
docker compose up -d   # MySQL 3306 + Redis 6379 + Kafka 9092
```

- 앱 **8081** 실행 (STS / `mvn spring-boot:run`)

## E2E 테스트 순서

| # | 폴더 | 요청 | 자동 저장 변수 |
|---|------|------|----------------|
| 1 | 1. 인증 | 회원가입 A, B | `userId`, `targetUserId` |
| 2 | 1. 인증 | 로그인 A | `accessToken`, `userId` |
| 3 | 1. 인증 | 로그인 B | `accessTokenB`, `targetUserId` |
| 4 | 3. 채팅 REST | 1:1 방 생성 | `roomId` |
| 5 | **ws-test.html** | 탭1: A 토큰 → Connect → JOIN | — |
| 6 | **ws-test.html** | 탭2: B 토큰 → Connect → JOIN | — |
| 7 | ws-test.html 탭1 | SEND | 탭2 로그에 `MESSAGE` |
| 8 | 3. 채팅 REST | 메시지 전송 (REST) | ws-test.html 탭2에 `MESSAGE` |
| 9 | 3. 채팅 REST | 메시지 목록 | DB 저장 확인 |

이미 가입된 계정이면 1~2번은 **로그인 A/B**만 실행하면 됩니다.

## WebSocket

Postman JSON Import는 **GET만** 표시됩니다. → **`ws-test.html`** 또는 [README-WebSocket.md](README-WebSocket.md)

## 컬렉션 변수

| 변수 | 설명 |
|------|------|
| `baseUrl` | `http://localhost:8081` |
| `wsBaseUrl` | `ws://localhost:8081` |
| `accessToken` | 사용자 A JWT |
| `accessTokenB` | 사용자 B JWT (A와 분리) |
| `userId` | A의 userId |
| `targetUserId` | B의 userId (1:1 상대) |
| `roomId` | 채팅방 ID |
| `searchNickname` | 기본 `사용자B` |

## REST Bearer

- 폴더 **3. 채팅 REST** 기본 Bearer = `accessToken` (A)
- **메시지 전송 (REST) — 사용자 B** 는 `accessTokenB` 사용

## 참고

- 프로토콜 상세: [`docs/WEBSOCKET.md`](../docs/WEBSOCKET.md)
- 체크리스트: 컬렉션 **5. 시나리오 체크리스트**
