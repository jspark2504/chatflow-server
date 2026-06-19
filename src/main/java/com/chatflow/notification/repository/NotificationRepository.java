package com.chatflow.notification.repository;

import com.chatflow.notification.domain.Notification;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface NotificationRepository extends ReactiveCrudRepository<Notification, Long> {

    /**
     * INSERT IGNORE: duplicate (message_id, receiver_id) silently skipped.
     * Ensures idempotency when Kafka redelivers the same event.
     */
    @Query("""
            INSERT IGNORE INTO notifications
                (receiver_id, sender_id, room_id, message_id, created_at)
            VALUES
                (:receiverId, :senderId, :roomId, :messageId, :createdAt)
            """)
    Mono<Void> insertIgnore(long receiverId, long senderId, long roomId, long messageId, Instant createdAt);
}
