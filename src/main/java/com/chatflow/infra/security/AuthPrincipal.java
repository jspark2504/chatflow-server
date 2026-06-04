package com.chatflow.infra.security;

public record AuthPrincipal(long userId, String email, String nickname) {
}
