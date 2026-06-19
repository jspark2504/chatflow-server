package com.chatflow.redis;

import com.chatflow.redis.dto.PresenceEvent;
import com.chatflow.websocket.ChatSessionRegistry;
import com.chatflow.websocket.dto.WsServerMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;

@Slf4j
@Component
@RequiredArgsConstructor
public class PresenceRedisSubscriber {

    private final ReactiveRedisMessageListenerContainer listenerContainer;
    private final ChatSessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;

    private Disposable subscription;

    @PostConstruct
    public void subscribe() {
        subscription = listenerContainer.receive(ChannelTopic.of(PresenceRedisChannel.CHANNEL))
                .doOnNext(msg -> handlePayload(msg.getMessage()))
                .doOnError(ex -> log.error("Redis presence subscription error", ex))
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
            PresenceEvent event = objectMapper.readValue(payload, PresenceEvent.class);
            WsServerMessage msg = event.online()
                    ? WsServerMessage.userOnline(event.userId())
                    : WsServerMessage.userOffline(event.userId());
            String json = objectMapper.writeValueAsString(msg);
            sessionRegistry.broadcastToAll(json).subscribe();
        } catch (Exception ex) {
            log.warn("Failed to handle Redis presence payload: {}", ex.getMessage());
        }
    }
}
