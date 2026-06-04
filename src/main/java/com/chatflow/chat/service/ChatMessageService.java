package com.chatflow.chat.service;

import com.chatflow.chat.domain.ChatMessage;
import com.chatflow.chat.dto.MessageResponse;
import com.chatflow.chat.dto.SendMessageRequest;
import com.chatflow.chat.repository.ChatMessageRepository;
import com.chatflow.common.error.BusinessException;
import com.chatflow.kafka.ChatMessageKafkaPublisher;
import com.chatflow.redis.ChatMessageRedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private static final int MAX_PAGE_SIZE = 200;

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;
    private final ChatMessageRedisPublisher chatMessageRedisPublisher;
    private final ChatMessageKafkaPublisher chatMessageKafkaPublisher;

    public Mono<MessageResponse> sendMessage(long roomId, long senderId, SendMessageRequest request) {
        String type = request.messageType() == null || request.messageType().isBlank()
                ? "TEXT"
                : request.messageType().trim();
        return chatRoomService.requireMember(roomId, senderId)
                .then(Mono.defer(() -> chatMessageRepository.save(ChatMessage.builder()
                        .roomId(roomId)
                        .senderId(senderId)
                        .messageType(type)
                        .messageContent(request.content().trim())
                        .isRead(false)
                        .createdAt(Instant.now())
                        .build())))
                .flatMap(saved -> {
                    if (saved.getId() == null) {
                        return Mono.error(new BusinessException(
                                HttpStatus.INTERNAL_SERVER_ERROR, "Message id missing after save"));
                    }
                    MessageResponse response = toResponse(saved);
                    return chatMessageRedisPublisher.publish(roomId, response)
                            .then(chatMessageKafkaPublisher.publish(roomId, response))
                            .thenReturn(response);
                });
    }

    public Flux<MessageResponse> listMessages(long roomId, long userId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        long offset = (long) safePage * safeSize;
        return chatRoomService.requireMember(roomId, userId)
                .thenMany(chatMessageRepository.findByRoomIdRecent(roomId, safeSize, offset))
                .map(this::toResponse);
    }

    private MessageResponse toResponse(ChatMessage message) {
        return new MessageResponse(
                message.getId(),
                message.getRoomId(),
                message.getSenderId(),
                message.getMessageType(),
                message.getMessageContent(),
                Boolean.TRUE.equals(message.getIsRead()),
                message.getCreatedAt());
    }
}
