package com.chatflow.redis;

import com.chatflow.redis.dto.ChatBroadcastEvent;
import com.chatflow.websocket.ChatSessionRegistry;
import com.chatflow.websocket.dto.WsServerMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageRedisSubscriber {

    private final ReactiveRedisMessageListenerContainer listenerContainer;
    private final ChatSessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;

    private Disposable subscription;

    @PostConstruct
    public void subscribe() {
        subscription = listenerContainer.receive(PatternTopic.of(ChatRoomRedisChannels.ROOM_PATTERN))
                .doOnNext(msg -> handlePayload(msg.getMessage()))
                .doOnError(ex -> log.error("Redis chat subscription error", ex))
                .subscribe();
    }

    @PreDestroy
    public void shutdown() {
        if (subscription != null) {
            subscription.dispose();
        }
    }

    private void handlePayload(String payload) {
        try {
            ChatBroadcastEvent event = objectMapper.readValue(payload, ChatBroadcastEvent.class);
            String outbound = objectMapper.writeValueAsString(WsServerMessage.message(event.message()));
            sessionRegistry.broadcast(event.roomId(), outbound).subscribe();
        } catch (Exception ex) {
            log.warn("Failed to handle Redis chat payload: {}", ex.getMessage());
        }
    }
}
