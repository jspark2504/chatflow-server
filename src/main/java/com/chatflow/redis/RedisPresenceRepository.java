package com.chatflow.redis;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis-backed global presence store.
 *
 * Keys:
 *   presence:online              — Redis SET of userId strings (globally online)
 *   presence:servers             — Redis SET of serverId strings (live server registry)
 *   presence:server:{serverId}   — Redis HASH { userId : sessionCount } per server
 *
 * WebSocketSession objects (not serializable) remain in ChatSessionRegistry (local).
 * This class manages only the "is this userId online on any server?" global truth.
 */
@Slf4j
@Component
public class RedisPresenceRepository {

    static final String ONLINE  = "presence:online";
    static final String SERVERS = "presence:servers";
    static final String SERVER_PREFIX = "presence:server:";

    private final ReactiveStringRedisTemplate redis;
    private final String serverId;

    public RedisPresenceRepository(
            ReactiveStringRedisTemplate redis,
            @Value("${server.address:localhost}:${server.port:8081}") String serverId) {
        this.redis = redis;
        this.serverId = serverId;
    }

    // ── lifecycle ──────────────────────────────────────────────────────────

    @PostConstruct
    void init() {
        cleanupStaleEntries()
                .doOnSuccess(v -> log.debug("[presence] startup cleanup done serverId={}", serverId))
                .doOnError(ex -> log.warn("[presence] startup cleanup failed", ex))
                .subscribe();
    }

    @PreDestroy
    void shutdown() {
        gracefulShutdown()
                .onErrorComplete()
                .block(Duration.ofSeconds(5));
        log.debug("[presence] graceful shutdown done serverId={}", serverId);
    }

    // ── public API ─────────────────────────────────────────────────────────

    /**
     * Called when a user opens a new WebSocket connection on this server.
     * Increments per-server session count and marks user globally online.
     */
    public Mono<Void> markOnline(long userId) {
        String uid = String.valueOf(userId);
        return redis.opsForHash().increment(myServerKey(), uid, 1L)
                .then(redis.opsForSet().add(ONLINE, uid))
                .then(redis.opsForSet().add(SERVERS, serverId))
                .then();
    }

    /**
     * Called when this server's LAST local session for userId disconnects.
     * Removes user from this server's hash; removes from global online set if
     * no other server still has the user.
     *
     * @return true if the user is now globally offline
     */
    public Mono<Boolean> removeLastSessionFromServer(long userId) {
        String uid = String.valueOf(userId);
        return redis.opsForHash().remove(myServerKey(), (Object) uid)
                .then(isOnAnotherServer(userId))
                .flatMap(stillElsewhere -> {
                    if (!stillElsewhere) {
                        return redis.opsForSet().remove(ONLINE, (Object) uid)
                                .thenReturn(true);
                    }
                    return Mono.just(false);
                });
    }

    /** O(1) global online check via Redis SISMEMBER. */
    public Mono<Boolean> isOnline(long userId) {
        return redis.opsForSet().isMember(ONLINE, String.valueOf(userId));
    }

    /** Returns the subset of candidates that are globally online. */
    public Mono<Set<Long>> getOnlineUserIds(Set<Long> candidates) {
        if (candidates.isEmpty()) {
            return Mono.just(Collections.emptySet());
        }
        return Flux.fromIterable(candidates)
                .filterWhen(this::isOnline)
                .collect(Collectors.toSet());
    }

    // ── private helpers ────────────────────────────────────────────────────

    private Mono<Boolean> isOnAnotherServer(long userId) {
        String uid = String.valueOf(userId);
        return redis.opsForSet().members(SERVERS)
                .filter(sid -> !sid.equals(serverId))
                .flatMap(sid -> redis.opsForHash().hasKey(serverKey(sid), uid))
                .filter(Boolean::booleanValue)
                .hasElements();
    }

    /**
     * On startup: remove stale entries left by a previous crash of this server instance.
     * Stable serverId (host:port) means we always find our own stale hash on restart.
     */
    private Mono<Void> cleanupStaleEntries() {
        String myKey = myServerKey();
        return redis.opsForHash().keys(myKey)
                .map(Object::toString)
                .collectList()
                .flatMap(staleUids -> {
                    if (staleUids.isEmpty()) {
                        return redis.opsForSet().add(SERVERS, serverId).then();
                    }
                    log.debug("[presence] cleaning {} stale uid(s) for serverId={}", staleUids.size(), serverId);
                    return redis.delete(myKey)
                            .then(Flux.fromIterable(staleUids)
                                    .flatMap(uid -> isOnAnotherServer(Long.parseLong(uid))
                                            .flatMap(alive -> alive
                                                    ? Mono.empty()
                                                    : redis.opsForSet().remove(ONLINE, (Object) uid).then()))
                                    .then())
                            .then(redis.opsForSet().add(SERVERS, serverId))
                            .then();
                });
    }

    /** On graceful shutdown: clean up our hash and unregister from the server set. */
    private Mono<Void> gracefulShutdown() {
        String myKey = myServerKey();
        return redis.opsForHash().keys(myKey)
                .map(Object::toString)
                .collectList()
                .flatMap(uids ->
                        redis.delete(myKey)
                                .then(Flux.fromIterable(uids)
                                        .flatMap(uid -> isOnAnotherServer(Long.parseLong(uid))
                                                .flatMap(alive -> alive
                                                        ? Mono.empty()
                                                        : redis.opsForSet().remove(ONLINE, (Object) uid).then()))
                                        .then())
                                .then(redis.opsForSet().remove(SERVERS, (Object) serverId))
                                .then());
    }

    private String myServerKey() {
        return SERVER_PREFIX + serverId;
    }

    private String serverKey(String sid) {
        return SERVER_PREFIX + sid;
    }
}
