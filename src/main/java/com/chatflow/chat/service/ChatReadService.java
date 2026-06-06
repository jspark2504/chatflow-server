package com.chatflow.chat.service;

import com.chatflow.chat.repository.ChatMessageRepository;
import com.chatflow.chat.repository.ChatRoomMemberRepository;
import com.chatflow.common.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ChatReadService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;

    public Mono<Void> markRead(long roomId, long userId, long lastReadMessageId) {
        return requireMember(roomId, userId)
                .then(chatMessageRepository.findById(lastReadMessageId)
                        .filter(message -> roomId == message.getRoomId())
                        .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.NOT_FOUND, "Message not found in room")))
                        .then(updateLastReadMessageId(roomId, userId, lastReadMessageId)));
    }

    public Mono<Void> markReadToLatest(long roomId, long userId) {
        return requireMember(roomId, userId)
                .then(chatMessageRepository.findMaxMessageIdByRoomId(roomId)
                        .flatMap(maxId -> maxId == 0L
                                ? Mono.empty()
                                : updateLastReadMessageId(roomId, userId, maxId)));
    }

    public Mono<Long> countUnread(long roomId, long userId, Long lastReadMessageId) {
        return chatMessageRepository.countUnread(roomId, userId, lastReadMessageId);
    }

    private Mono<Void> updateLastReadMessageId(long roomId, long userId, long lastReadMessageId) {
        return chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.FORBIDDEN, "Not a member of this room")))
                .flatMap(member -> {
                    Long current = member.getLastReadMessageId();
                    if (current != null && lastReadMessageId <= current) {
                        return Mono.empty();
                    }
                    member.setLastReadMessageId(lastReadMessageId);
                    return chatRoomMemberRepository.save(member);
                })
                .then();
    }

    private Mono<Void> requireMember(long roomId, long userId) {
        return chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, userId)
                .flatMap(member -> member
                        ? Mono.empty()
                        : Mono.error(new BusinessException(HttpStatus.FORBIDDEN, "Not a member of this room")));
    }
}
