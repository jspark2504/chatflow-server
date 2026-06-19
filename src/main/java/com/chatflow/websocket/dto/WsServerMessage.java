package com.chatflow.websocket.dto;

import com.chatflow.chat.dto.MessageResponse;

public record WsServerMessage(
        WsServerMessageType type,
        Long roomId,
        MessageResponse message,
        String error,
        Long userId
) {

    public static WsServerMessage joined(long roomId) {
        return new WsServerMessage(WsServerMessageType.JOINED, roomId, null, null, null);
    }

    public static WsServerMessage left(long roomId) {
        return new WsServerMessage(WsServerMessageType.LEFT, roomId, null, null, null);
    }

    public static WsServerMessage message(MessageResponse message) {
        return new WsServerMessage(WsServerMessageType.MESSAGE, message.roomId(), message, null, null);
    }

    public static WsServerMessage error(String error) {
        return new WsServerMessage(WsServerMessageType.ERROR, null, null, error, null);
    }

    public static WsServerMessage userOnline(long userId) {
        return new WsServerMessage(WsServerMessageType.USER_ONLINE, null, null, null, userId);
    }

    public static WsServerMessage userOffline(long userId) {
        return new WsServerMessage(WsServerMessageType.USER_OFFLINE, null, null, null, userId);
    }
}
