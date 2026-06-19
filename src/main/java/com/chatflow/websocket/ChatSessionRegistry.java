package com.chatflow.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatSessionRegistry {

    // room-level: roomId → sessions in that room
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    // room-level: sessionId → rooms joined
    private final ConcurrentHashMap<String, Set<Long>> sessionRooms = new ConcurrentHashMap<>();

    // user-level: userId → all sessions of that user (multi-tab support)
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    // user-level: sessionId → userId (reverse lookup)
    private final ConcurrentHashMap<String, Long> sessionUser = new ConcurrentHashMap<>();
    // global: all connected sessions (for presence broadcast)
    private final Set<WebSocketSession> globalSessions = ConcurrentHashMap.newKeySet();

    // ── user presence ──────────────────────────────────────────────────────

    public void registerUser(long userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, id -> ConcurrentHashMap.newKeySet()).add(session);
        sessionUser.put(session.getId(), userId);
        globalSessions.add(session);
    }

    public boolean isOnline(long userId) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    public Set<Long> getOnlineUserIds(Set<Long> candidates) {
        return candidates.stream()
                .filter(this::isOnline)
                .collect(java.util.stream.Collectors.toSet());
    }

    /** 현재 연결된 모든 세션에 브로드캐스트 (프레즌스 이벤트용) */
    public Mono<Void> broadcastToAll(String json) {
        return Flux.fromIterable(globalSessions)
                .filter(WebSocketSession::isOpen)
                .flatMap(session -> session.send(Mono.just(session.textMessage(json)))
                        .onErrorResume(ex -> Mono.empty()))
                .then();
    }

    // ── room membership ────────────────────────────────────────────────────

    public void join(long roomId, WebSocketSession session) {
        roomSessions.computeIfAbsent(roomId, id -> ConcurrentHashMap.newKeySet()).add(session);
        sessionRooms.computeIfAbsent(session.getId(), id -> ConcurrentHashMap.newKeySet()).add(roomId);
    }

    public void leave(long roomId, WebSocketSession session) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) roomSessions.remove(roomId, sessions);
        }
        Set<Long> rooms = sessionRooms.get(session.getId());
        if (rooms != null) {
            rooms.remove(roomId);
            if (rooms.isEmpty()) sessionRooms.remove(session.getId(), rooms);
        }
    }

    public void removeSession(WebSocketSession session) {
        // room cleanup
        Set<Long> rooms = sessionRooms.remove(session.getId());
        if (rooms != null) {
            for (Long roomId : rooms) {
                Set<WebSocketSession> sessions = roomSessions.get(roomId);
                if (sessions != null) {
                    sessions.remove(session);
                    if (sessions.isEmpty()) roomSessions.remove(roomId, sessions);
                }
            }
        }
        // user presence cleanup
        globalSessions.remove(session);
        Long userId = sessionUser.remove(session.getId());
        if (userId != null) {
            Set<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) userSessions.remove(userId, sessions);
            }
        }
    }

    /** 특정 방에 JOIN한 세션들에게 브로드캐스트 (채팅 메시지용) */
    public Mono<Void> broadcast(long roomId, String json) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null || sessions.isEmpty()) return Mono.empty();
        return Flux.fromIterable(sessions)
                .filter(WebSocketSession::isOpen)
                .flatMap(session -> session.send(Mono.just(session.textMessage(json)))
                        .onErrorResume(ex -> Mono.empty()))
                .then();
    }
}
