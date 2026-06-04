package com.chatflow.chat.dto;

import java.time.Instant;

public record MessageResponse(
        long messageId,
        long roomId,
        long senderId,
        String messageType,
        String content,
        boolean read,
        Instant createdAt
) {
}
