package com.chatflow.infra.security;

import com.chatflow.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties properties;
    private final TokenBlacklistService tokenBlacklistService;

    public String createToken(long userId, String email, String nickname) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + properties.getExpirationMs());
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
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
        return toPrincipal(parse(token));
    }

    public Mono<AuthPrincipal> authenticateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return Mono.empty();
        }
        try {
            Claims claims = parse(token);
            String blacklistKey = blacklistKey(claims, token);
            return tokenBlacklistService.isBlacklisted(blacklistKey)
                    .flatMap(blacklisted -> {
                        if (Boolean.TRUE.equals(blacklisted)) {
                            return Mono.error(new BadCredentialsException("Token revoked"));
                        }
                        return Mono.just(toPrincipal(claims));
                    });
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException ex) {
            return Mono.empty();
        }
    }

    public Mono<Void> revokeToken(String token) {
        Claims claims = parse(token);
        String blacklistKey = blacklistKey(claims, token);
        Instant exp = claims.getExpiration().toInstant();
        Duration ttl = Duration.between(Instant.now(), exp);
        return tokenBlacklistService.blacklist(blacklistKey, ttl);
    }

    private AuthPrincipal toPrincipal(Claims claims) {
        long userId = Long.parseLong(claims.getSubject());
        String email = claims.get("email", String.class);
        String nickname = claims.get("nickname", String.class);
        if (nickname == null) {
            nickname = claims.get("username", String.class);
        }
        return new AuthPrincipal(userId, email, nickname);
    }

    private String blacklistKey(Claims claims, String rawToken) {
        String jti = claims.getId();
        if (StringUtils.hasText(jti)) {
            return jti;
        }
        return hashToken(rawToken);
    }

    private static String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private SecretKey signingKey() {
        byte[] keyBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
