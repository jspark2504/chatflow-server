package com.chatflow.chat.repository;

import com.chatflow.chat.domain.ChatRoom;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatRoomRepository extends ReactiveCrudRepository<ChatRoom, Long> {

    @Query("""
            SELECT cr.* FROM chat_room cr
            WHERE cr.id IN (SELECT room_id FROM chat_room_member WHERE user_id = :userId)
            ORDER BY cr.created_at DESC
            """)
    Flux<ChatRoom> findAllByMemberUserId(long userId);

    @Query("""
            SELECT cr.id FROM chat_room cr
            WHERE (SELECT COUNT(*) FROM chat_room_member m WHERE m.room_id = cr.id) = 2
              AND EXISTS (SELECT 1 FROM chat_room_member m WHERE m.room_id = cr.id AND m.user_id = :userId1)
              AND EXISTS (SELECT 1 FROM chat_room_member m WHERE m.room_id = cr.id AND m.user_id = :userId2)
            LIMIT 1
            """)
    Mono<Long> findDirectRoomId(long userId1, long userId2);
}
