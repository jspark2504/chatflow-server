package com.chatflow.redis;

public final class ChatRoomRedisChannels {

    public static final String ROOM_PATTERN = "chat:room:*";

    private ChatRoomRedisChannels() {
    }

    public static String room(long roomId) {
        return "chat:room:" + roomId;
    }
}
