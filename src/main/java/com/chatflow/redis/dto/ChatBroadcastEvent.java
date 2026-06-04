package com.chatflow.redis.dto;

import com.chatflow.chat.dto.MessageResponse;

public record ChatBroadcastEvent(long roomId, MessageResponse message) {
}
