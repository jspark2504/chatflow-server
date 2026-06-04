package com.chatflow.websocket.dto;

import com.chatflow.chat.dto.MessageResponse;

public record WsServerMessage(
        WsServerMessageType type,
        Long roomId,
        MessageResponse message,
        String error
) {

    public static WsServerMessage joined(long roomId) {
        return new WsServerMessage(WsServerMessageType.JOINED, roomId, null, null);
    }

    public static WsServerMessage left(long roomId) {
        return new WsServerMessage(WsServerMessageType.LEFT, roomId, null, null);
    }

    public static WsServerMessage message(MessageResponse message) {
        return new WsServerMessage(WsServerMessageType.MESSAGE, message.roomId(), message, null);
    }

    public static WsServerMessage error(String error) {
        return new WsServerMessage(WsServerMessageType.ERROR, null, null, error);
    }
}
