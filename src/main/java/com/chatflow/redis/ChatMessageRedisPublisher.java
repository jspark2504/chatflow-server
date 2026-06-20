package com.chatflow.redis;

import com.chatflow.chat.dto.MessageResponse;
import com.chatflow.redis.dto.ChatBroadcastEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ChatMessageRedisPublisher {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public Mono<Void> publish(long roomId, MessageResponse message) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(new ChatBroadcastEvent(roomId, message)))
                .flatMap(json -> redisTemplate.convertAndSend(ChatRoomRedisChannels.room(roomId), json))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                        .maxBackoff(Duration.ofSeconds(1))
                        .filter(ex -> !(ex instanceof JsonProcessingException)))
                .then();
    }
}
