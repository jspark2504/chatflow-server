# Kafka (5단계)

## 역할

| 경로 | 용도 |
|------|------|
| **Redis Pub/Sub** | 실시간 WebSocket 브로드캐스트 (유지) |
| **Kafka** | 메시지 이벤트 기록·다운스트림 확장 (알림, 분석, 다른 서비스) |

메시지 저장 후 **Redis + Kafka 둘 다** publish 합니다.

## 로컬 실행

```bash
docker compose up -d   # mysql + redis + kafka
```

- Kafka: `localhost:9092`
- 토픽: `chat.message.sent` (첫 publish 시 자동 생성)
- Zookeeper 없음 (KRaft 단일 브로커)

## 흐름

```
REST/WS SEND → DB 저장
            → Redis chat:room:{id}  → WS MESSAGE
            → Kafka chat.message.sent → Consumer 로그 (확장 지점)
```

## 확인

1. 앱 재시작 후 메시지 전송 (REST 또는 WS SEND)
2. 로그에서:
   ```
   Kafka consumed chat.message.sent roomId=3 messageId=... senderId=...
   ```

## 장애

- Kafka가 꺼져 있으면 publish는 warn 로그만 남기고 **REST/WS·Redis는 동작** (비동기 fire-and-forget).
