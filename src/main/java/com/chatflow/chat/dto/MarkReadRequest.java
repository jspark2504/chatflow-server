package com.chatflow.chat.dto;

import jakarta.validation.constraints.NotNull;

public record MarkReadRequest(
        @NotNull Long lastReadMessageId
) {
}
