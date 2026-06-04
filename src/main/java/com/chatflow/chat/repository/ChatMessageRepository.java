package com.chatflow.chat.repository;

import com.chatflow.chat.domain.ChatMessage;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ChatMessageRepository extends ReactiveCrudRepository<ChatMessage, Long> {

    @Query("""
            SELECT * FROM chat_message
            WHERE room_id = :roomId
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
            """)
    Flux<ChatMessage> findByRoomIdRecent(long roomId, int limit, long offset);
}
