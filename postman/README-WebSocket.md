# WebSocket 테스트 가이드

## Postman에서 GET만 보이는 이유

**Postman 컬렉션 JSON(v2.1)은 WebSocket을 지원하지 않습니다.**  
Import하면 `ws://` 요청이 전부 **HTTP GET**으로 바뀌고, **Send**만 보이며 `Invalid protocol: ws:` 가 납니다. (Postman 공식 제한 — WS 전용 컬렉션도 UI에서 **직접 만들어야** 합니다.)

→ **JSON Import로 WebSocket 컬렉션을 만드는 방식은 사용하지 마세요.**

---

## 방법 1 — 브라우저 (가장 빠름) ✅

1. 서버·Redis 실행
2. REST로 로그인 → `accessToken` 복사 ([Chatflow-Server](Chatflow-Server.postman_collection.json) 컬렉션)
3. 파일 더블클릭: **`postman/ws-test.html`**
4. token · roomId 입력 → **Connect** → **JOIN** → **SEND**
5. 실시간 수신 확인: **탭 2개** 열어서 B 토큰으로 동일하게 JOIN 후 A가 SEND

---

## 방법 2 — PowerShell

```powershell
# A: JOIN 후 대기는 send로 메시지내며 수신 확인
.\scripts\ws-chat.ps1 -Token "여기_JWT" -RoomId 1 -Action join
.\scripts\ws-chat.ps1 -Token "여기_JWT" -RoomId 1 -Action send -Content "안녕"
```

---

## 방법 3 — Postman 수동 (Connect 버튼 나옴)

1. Postman **New** → **WebSocket** (⚠️ HTTP Request 아님)
2. URL:
   ```
   ws://localhost:8081/ws/chat?token=여기_accessToken
   ```
3. **Connect** 클릭 (GET Send 아님)
4. Message 작성 후 Send:

```json
{"type":"JOIN","roomId":1}
```

```json
{"type":"SEND","roomId":1,"content":"안녕","messageType":"TEXT"}
```

5. (선택) **Save** → 새 컬렉션 이름 예: `Chatflow WS` — **WebSocket만** 담는 컬렉션으로 저장
6. B 사용자: **새 WebSocket 탭** + B 토큰으로 ②번 URL

### accessToken 받기

`Chatflow-Server` 컬렉션 → **로그인 — 사용자 A** → Tests가 변수에 저장  
또는 Variables에 직접 붙여넣기.

---

## REST 컬렉션 (로그인·방·메시지)

| 파일 | 용도 |
|------|------|
| `Chatflow-Server.postman_collection.json` | HTTP API |
| `local.postman_environment.json` | 변수 |

`Chatflow-WebSocket.postman_collection.json` 은 **참고용**이며, Import 시 GET으로 보일 수 있습니다. 실제 WS 테스트는 **ws-test.html** 또는 Postman **New → WebSocket** 을 사용하세요.

프로토콜: [`docs/WEBSOCKET.md`](../docs/WEBSOCKET.md)
