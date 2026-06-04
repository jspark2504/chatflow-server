package com.chatflow.websocket;

import com.chatflow.chat.dto.SendMessageRequest;
import com.chatflow.chat.service.ChatMessageService;
import com.chatflow.chat.service.ChatRoomService;
import com.chatflow.common.error.BusinessException;
import com.chatflow.infra.security.AuthPrincipal;
import com.chatflow.infra.security.JwtService;
import com.chatflow.websocket.dto.WsClientMessage;
import com.chatflow.websocket.dto.WsClientMessageType;
import com.chatflow.websocket.dto.WsServerMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    private final JwtService jwtService;
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final ChatSessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        AuthPrincipal principal = authenticate(session);
        if (principal == null) {
            return sendError(session, "Invalid or missing token")
                    .then(session.close());
        }

        long userId = principal.userId();
        return session.receive()
                .map(msg -> msg.getPayloadAsText())
                .concatMap(text -> handleInbound(session, userId, text))
                .doFinally(signal -> sessionRegistry.removeSession(session))
                .then();
    }

    private Mono<Void> handleInbound(WebSocketSession session, long userId, String text) {
        if (!StringUtils.hasText(text)) {
            return Mono.empty();
        }
        try {
            WsClientMessage inbound = objectMapper.readValue(text, WsClientMessage.class);
            if (inbound.type() == null) {
                return sendError(session, "type is required");
            }
            return switch (inbound.type()) {
                case JOIN -> handleJoin(session, userId, inbound);
                case SEND -> handleSend(session, userId, inbound);
                case LEAVE -> handleLeave(session, userId, inbound);
            };
        } catch (Exception ex) {
            return sendError(session, "Invalid message format");
        }
    }

    private Mono<Void> handleJoin(WebSocketSession session, long userId, WsClientMessage inbound) {
        Long roomId = inbound.roomId();
        if (roomId == null) {
            return sendError(session, "roomId is required for JOIN");
        }
        return chatRoomService.requireMember(roomId, userId)
                .then(Mono.fromRunnable(() -> sessionRegistry.join(roomId, session)))
                .then(send(session, WsServerMessage.joined(roomId)));
    }

    private Mono<Void> handleLeave(WebSocketSession session, long userId, WsClientMessage inbound) {
        Long roomId = inbound.roomId();
        if (roomId == null) {
            return sendError(session, "roomId is required for LEAVE");
        }
        return chatRoomService.requireMember(roomId, userId)
                .then(Mono.fromRunnable(() -> sessionRegistry.leave(roomId, session)))
                .then(send(session, WsServerMessage.left(roomId)));
    }

    private Mono<Void> handleSend(WebSocketSession session, long userId, WsClientMessage inbound) {
        Long roomId = inbound.roomId();
        if (roomId == null) {
            return sendError(session, "roomId is required for SEND");
        }
        if (!StringUtils.hasText(inbound.content())) {
            return sendError(session, "content is required for SEND");
        }
        SendMessageRequest request = new SendMessageRequest(
                inbound.content().trim(),
                inbound.messageType());
        return chatMessageService.sendMessage(roomId, userId, request)
                .then()
                .onErrorResume(BusinessException.class, ex -> sendError(session, ex.getMessage()));
    }

    private Mono<Void> sendError(WebSocketSession session, String message) {
        return send(session, WsServerMessage.error(message));
    }

    private Mono<Void> send(WebSocketSession session, WsServerMessage payload) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(payload))
                .flatMap(json -> session.send(Mono.just(session.textMessage(json))));
    }

    private AuthPrincipal authenticate(WebSocketSession session) {
        String token = resolveToken(session);
        if (!StringUtils.hasText(token)) {
            return null;
        }
        try {
            return jwtService.parsePrincipal(token);
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    private static String resolveToken(WebSocketSession session) {
        var query = UriComponentsBuilder.fromUri(session.getHandshakeInfo().getUri()).build().getQueryParams();
        String token = query.getFirst("token");
        if (StringUtils.hasText(token)) {
            return token;
        }
        token = query.getFirst("access_token");
        if (StringUtils.hasText(token)) {
            return token;
        }
        String auth = session.getHandshakeInfo().getHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }
}
