package com.chatflow.infra.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "chatflow.jwt")
public class JwtProperties {

    private String secret = "change-me";
    private long expirationMs = 86_400_000L;
}
