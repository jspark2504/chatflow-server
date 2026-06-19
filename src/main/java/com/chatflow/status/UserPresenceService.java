package com.chatflow.status;

import com.chatflow.websocket.ChatSessionRegistry;
import com.chatflow.websocket.dto.WsServerMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserPresenceService {

    private final ChatSessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;

    public Mono<Void> broadcastOnline(long userId) {
        return broadcast(WsServerMessage.userOnline(userId));
    }

    public Mono<Void> broadcastOffline(long userId) {
        return broadcast(WsServerMessage.userOffline(userId));
    }

    private Mono<Void> broadcast(WsServerMessage msg) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(msg))
                .flatMap(sessionRegistry::broadcastToAll);
    }
}
