package com.chatflow.chat.dto;

import com.chatflow.chat.domain.RoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateRoomRequest(
        @NotNull RoomType type,
        Long targetUserId,
        @Size(max = 255) String roomName,
        List<Long> memberUserIds
) {
}
