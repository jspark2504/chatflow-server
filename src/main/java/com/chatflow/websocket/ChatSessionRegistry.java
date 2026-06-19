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

    /** True if this server still has at least one open session for the user. */
    public boolean hasLocalSessions(long userId) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
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

    /**
     * Removes all traces of this session (rooms + user presence).
     *
     * @return true if this was the last session for the user on this server,
     *         meaning the caller should update the global Redis presence state.
     */
    public boolean removeSession(WebSocketSession session) {
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
                if (sessions.isEmpty()) {
                    userSessions.remove(userId, sessions);
                    return true; // last session on this server
                }
            }
        }
        return false;
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
