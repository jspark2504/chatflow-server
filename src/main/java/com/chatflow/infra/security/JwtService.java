package com.chatflow.infra.security;

import com.chatflow.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties properties;

    public String createToken(long userId, String email, String nickname) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + properties.getExpirationMs());
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("nickname", nickname)
                .issuedAt(now)
                .expiration(exp)
                .signWith(signingKey())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public AuthPrincipal parsePrincipal(String token) {
        Claims claims = parse(token);
        long userId = Long.parseLong(claims.getSubject());
        String email = claims.get("email", String.class);
        String nickname = claims.get("nickname", String.class);
        if (nickname == null) {
            nickname = claims.get("username", String.class);
        }
        return new AuthPrincipal(userId, email, nickname);
    }

    private SecretKey signingKey() {
        byte[] keyBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
