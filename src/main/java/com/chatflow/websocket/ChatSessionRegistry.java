package com.chatflow.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatSessionRegistry {

    private final ConcurrentHashMap<Long, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<Long>> sessionRooms = new ConcurrentHashMap<>();

    public void join(long roomId, WebSocketSession session) {
        roomSessions.computeIfAbsent(roomId, id -> ConcurrentHashMap.newKeySet()).add(session);
        sessionRooms.computeIfAbsent(session.getId(), id -> ConcurrentHashMap.newKeySet()).add(roomId);
    }

    public void leave(long roomId, WebSocketSession session) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(roomId, sessions);
            }
        }
        Set<Long> rooms = sessionRooms.get(session.getId());
        if (rooms != null) {
            rooms.remove(roomId);
            if (rooms.isEmpty()) {
                sessionRooms.remove(session.getId(), rooms);
            }
        }
    }

    public void removeSession(WebSocketSession session) {
        Set<Long> rooms = sessionRooms.remove(session.getId());
        if (rooms == null) {
            return;
        }
        for (Long roomId : rooms) {
            Set<WebSocketSession> sessions = roomSessions.get(roomId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    roomSessions.remove(roomId, sessions);
                }
            }
        }
    }

    public Mono<Void> broadcast(long roomId, String json) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null || sessions.isEmpty()) {
            return Mono.empty();
        }
        return Flux.fromIterable(sessions)
                .filter(WebSocketSession::isOpen)
                .flatMap(session -> session.send(Mono.just(session.textMessage(json)))
                        .onErrorResume(ex -> Mono.empty()))
                .then();
    }
}
