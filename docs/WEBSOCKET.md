# WebSocket 실시간 채팅 (3단계)

## 연결

### 권장 (브라우저)

```
ws://localhost:8081/ws/chat
Sec-WebSocket-Protocol: access_token.{accessToken}
```

JavaScript:

```javascript
const token = loginResponse.accessToken;
const ws = new WebSocket("ws://localhost:8081/ws/chat", [`access_token.${token}`]);
```

- 로그인(`POST /api/auth/login`) 응답의 `accessToken` 사용
- queryString(`?token=`)은 **deprecated** — 로컬 테스트·Postman 호환용으로만 유지

### 비브라우저 클라이언트

- 핸드셰이크 `Authorization: Bearer {accessToken}` 지원
- `?token=` / `?access_token=` 쿼리도 지원 (레거시)

## 클라이언트 → 서버

| type | 필수 필드 | 설명 |
|------|-----------|------|
| `JOIN` | `roomId` | 방 구독 (멤버만 가능, 입장 시 최신까지 읽음 처리) |
| `SEND` | `roomId`, `content` | 메시지 저장 + Redis Pub/Sub |
| `LEAVE` | `roomId` | 구독 해제 |

```json
{"type":"JOIN","roomId":1}
{"type":"SEND","roomId":1,"content":"안녕","messageType":"TEXT"}
{"type":"LEAVE","roomId":1}
```

## 서버 → 클라이언트

| type | 설명 |
|------|------|
| `JOINED` | `roomId` — JOIN 성공 |
| `LEFT` | `roomId` — LEAVE 완료 |
| `MESSAGE` | `message` — 새 메시지 (REST 전송 포함, 동일 포맷) |
| `ERROR` | `error` — 오류 메시지 |

`MESSAGE` 예시:

```json
{
  "type": "MESSAGE",
  "roomId": 1,
  "message": {
    "messageId": 10,
    "roomId": 1,
    "senderId": 2,
    "messageType": "TEXT",
    "content": "안녕",
    "read": false,
    "createdAt": "2026-06-04T12:00:00Z"
  },
  "error": null
}
```

## Redis Pub/Sub

- 채널: `chat:room:{roomId}`
- 인스턴스 간 브로드캐스트용 (수평 확장 시 동일 패턴)
- 로컬: `docker compose up -d` 로 Redis **6379** 필요

## 테스트 순서

1. Redis + MySQL + 앱 실행
2. 두 사용자 로그인, 1:1 방 생성
3. Postman WebSocket 또는 [websocat](https://github.com/vi/websocat)으로 A·B 각각 연결
4. 양쪽 `JOIN` 후 한쪽 `SEND` → 다른 쪽 `MESSAGE` 수신 확인
5. REST `POST .../messages` 로 보낸 메시지도 WS로 수신되는지 확인
