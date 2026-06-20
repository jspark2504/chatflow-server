package com.chatflow.chat.repository;

import com.chatflow.chat.domain.ChatMessage;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface ChatMessageRepository extends ReactiveCrudRepository<ChatMessage, Long> {

    @Query("""
            SELECT * FROM chat_message
            WHERE room_id = :roomId
            ORDER BY id DESC
            LIMIT :limit
            """)
    Flux<ChatMessage> findLatestByRoomId(long roomId, int limit);

    @Query("""
            SELECT * FROM chat_message
            WHERE room_id = :roomId AND id < :beforeMessageId
            ORDER BY id DESC
            LIMIT :limit
            """)
    Flux<ChatMessage> findByRoomIdBeforeMessageId(long roomId, long beforeMessageId, int limit);

    @Query("""
            SELECT COUNT(*) FROM chat_message
            WHERE room_id = :roomId
              AND id > COALESCE(:lastReadMessageId, 0)
              AND sender_id <> :userId
            """)
    Mono<Long> countUnread(long roomId, long userId, Long lastReadMessageId);

    @Query("""
            SELECT COALESCE(MAX(id), 0) FROM chat_message
            WHERE room_id = :roomId
            """)
    Mono<Long> findMaxMessageIdByRoomId(long roomId);

    @Query("""
            SELECT * FROM chat_message
            WHERE room_id = :roomId AND sender_id = :senderId AND client_id = :clientId
            """)
    Mono<ChatMessage> findByRoomIdAndSenderIdAndClientId(long roomId, long senderId, String clientId);

    @Modifying
    @Query("UPDATE chat_message SET published_at = :publishedAt WHERE id = :id")
    Mono<Integer> markAsPublished(Long id, Instant publishedAt);

    // TODO: 멀티 인스턴스 전환 시 FOR UPDATE SKIP LOCKED + 트랜잭션으로 교체 필요
    @Query("""
            SELECT * FROM chat_message
            WHERE published_at IS NULL
              AND publish_failed_at IS NULL
              AND retry_count < :maxRetry
              AND created_at > :since
            ORDER BY id ASC
            LIMIT :limit
            """)
    Flux<ChatMessage> findUnpublished(int maxRetry, Instant since, int limit);

    @Modifying
    @Query("UPDATE chat_message SET retry_count = retry_count + 1 WHERE id = :id")
    Mono<Integer> incrementRetryCount(Long id);

    @Modifying
    @Query("UPDATE chat_message SET publish_failed_at = :failedAt WHERE id = :id")
    Mono<Integer> markAsFailed(Long id, Instant failedAt);
}
