package com.chatflow.chat.dto;

public record SendMessageResult(MessageResponse response, boolean isDuplicate) {
}
