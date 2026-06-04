package com.chatflow.kafka.dto;

import com.chatflow.chat.dto.MessageResponse;

import java.time.Instant;

/**
 * Kafka에 기록하는 채팅 메시지 이벤트 (실시간 WS는 Redis 경로 유지).
 */
public record ChatMessageSentEvent(
        long roomId,
        MessageResponse message,
        Instant publishedAt
) {
}
