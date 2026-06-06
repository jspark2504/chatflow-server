package com.chatflow.chat.dto;

import java.util.List;

public record MessagePageResponse(
        List<MessageResponse> messages,
        boolean hasMore,
        Long nextCursor
) {
}
