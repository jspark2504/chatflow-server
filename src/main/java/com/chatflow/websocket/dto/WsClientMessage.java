package com.chatflow.websocket.dto;

public record WsClientMessage(
        WsClientMessageType type,
        Long roomId,
        String content,
        String messageType
) {
}
