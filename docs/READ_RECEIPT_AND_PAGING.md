# 읽음 처리 (Read Receipt)

## DB

`chat_room_member.last_read_message_id` — 사용자별 마지막 읽은 메시지

## API

### 읽음 갱신

```
PUT /api/chat/rooms/{roomId}/read
Authorization: Bearer {token}

{ "lastReadMessageId": 123 }
```

→ `204 No Content`

### 방 목록/상세

`RoomResponse`에 추가:

| 필드 | 설명 |
|------|------|
| `unreadCount` | 내가 안 읽은 메시지 수 (본인 발신 제외) |
| `lastReadMessageId` | 내 마지막 읽은 메시지 ID (null 가능) |

## WebSocket

`JOIN` 성공 시 해당 방의 **최신 메시지까지 자동 읽음** 처리.

## 안읽음 계산

```sql
COUNT(*) FROM chat_message
WHERE room_id = ?
  AND id > COALESCE(last_read_message_id, 0)
  AND sender_id <> {me}
```

## Cursor Paging (메시지 목록)

```
GET /api/chat/rooms/{roomId}/messages?size=20&beforeMessageId=99980
```

| 파라미터 | 설명 |
|----------|------|
| `size` | 페이지 크기 (기본 50, 최대 200) |
| `beforeMessageId` | 생략 시 최신 N건, 있으면 해당 ID 미만(더 과거) |

응답:

```json
{
  "messages": [ ... ],
  "hasMore": true,
  "nextCursor": 99961
}
```

- `messages`: 최신순 (messageId DESC)
- `nextCursor`: 다음 요청에 `beforeMessageId`로 전달

## Security

- `POST /api/auth/signup`, `/api/auth/login` — permitAll
- `/api/**` — `authenticated()` (Bearer JWT)
- `/ws/**` — permitAll, 핸드셰이크에서 JWT 검증
