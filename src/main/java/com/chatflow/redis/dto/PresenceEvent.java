package com.chatflow.redis.dto;

public record PresenceEvent(long userId, boolean online) {
}
