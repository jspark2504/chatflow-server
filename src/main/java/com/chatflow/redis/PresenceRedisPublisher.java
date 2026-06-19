package com.chatflow.redis;

import com.chatflow.redis.dto.PresenceEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class PresenceRedisPublisher {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public Mono<Void> publish(long userId, boolean online) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(new PresenceEvent(userId, online)))
                .flatMap(json -> redisTemplate.convertAndSend(PresenceRedisChannel.CHANNEL, json))
                .then();
    }
}
