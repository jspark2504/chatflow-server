package com.chatflow.user.dto;

public record LoginResponse(String token, long userId, String username) {
}
