package com.chatflow.infra.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "auth:blacklist:";

    private final ReactiveStringRedisTemplate redis;

    public Mono<Void> blacklist(String key, Duration ttl) {
        if (ttl.isZero() || ttl.isNegative()) {
            return Mono.empty();
        }
        return redis.opsForValue().set(KEY_PREFIX + key, "1", ttl).then();
    }

    public Mono<Boolean> isBlacklisted(String key) {
        return redis.hasKey(KEY_PREFIX + key);
    }
}
