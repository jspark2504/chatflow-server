package com.chatflow.auth.dto;

public record LoginResponse(long userId, String email, String nickname, String accessToken) {
}
