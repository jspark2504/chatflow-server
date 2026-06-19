package com.chatflow.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotBlank @Size(max = 10000) String content,
        @Size(max = 50) String messageType,
        @Size(max = 64) String clientId
) {
}
