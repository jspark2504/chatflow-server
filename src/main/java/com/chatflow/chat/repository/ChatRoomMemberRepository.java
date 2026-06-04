package com.chatflow.chat.repository;

import com.chatflow.chat.domain.ChatRoomMember;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatRoomMemberRepository extends ReactiveCrudRepository<ChatRoomMember, Long> {

    Flux<ChatRoomMember> findByRoomId(long roomId);

    Mono<Boolean> existsByRoomIdAndUserId(long roomId, long userId);

    @Query("SELECT COUNT(*) FROM chat_room_member WHERE room_id = :roomId")
    Mono<Long> countByRoomId(long roomId);
}
