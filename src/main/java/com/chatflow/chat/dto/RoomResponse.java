package com.chatflow.chat.dto;

import com.chatflow.chat.domain.RoomType;

import java.time.Instant;
import java.util.List;

public record RoomResponse(
        long roomId,
        String roomName,
        RoomType type,
        int memberCount,
        Instant createdAt,
        List<RoomMemberResponse> members
) {
}
