package com.chatflow.user.dto;

public record LoginResponse(long userId, String email, String nickname, String accessToken) {
}
