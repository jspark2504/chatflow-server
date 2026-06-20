package com.chatflow.chat.outbox;

import com.chatflow.chat.domain.ChatMessage;
import com.chatflow.chat.dto.MessageResponse;
import com.chatflow.chat.repository.ChatMessageRepository;
import com.chatflow.redis.ChatMessageRedisPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private static final int MAX_RETRY = 5;
    private static final int SCAN_WINDOW_MINUTES = 10;
    private static final int BATCH_LIMIT = 100;

    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageRedisPublisher chatMessageRedisPublisher;

    @Scheduled(fixedDelay = 30_000)
    public void republishUnpublished() {
        Instant since = Instant.now().minus(SCAN_WINDOW_MINUTES, ChronoUnit.MINUTES);

        chatMessageRepository.findUnpublished(MAX_RETRY, since, BATCH_LIMIT)
                .concatMap(this::tryRepublish)
                .doOnError(e -> log.error("Outbox scan error", e))
                .onErrorComplete()
                .subscribe();
    }

    private reactor.core.publisher.Mono<Void> tryRepublish(ChatMessage message) {
        MessageResponse response = toResponse(message);

        return chatMessageRedisPublisher.publish(message.getRoomId(), response)
                .then(chatMessageRepository.markAsPublished(message.getId(), Instant.now()))
                .doOnSuccess(rows -> log.info("Outbox: republished messageId={} roomId={}",
                        message.getId(), message.getRoomId()))
                .then()
                .onErrorResume(e -> {
                    log.warn("Outbox: publish failed messageId={} retryCount={}",
                            message.getId(), message.getRetryCount(), e);

                    boolean lastAttempt = message.getRetryCount() >= MAX_RETRY - 1;
                    if (lastAttempt) {
                        return chatMessageRepository.markAsFailed(message.getId(), Instant.now())
                                .doOnSuccess(r -> log.error(
                                        "Outbox: permanently failed messageId={} after {} retries",
                                        message.getId(), MAX_RETRY))
                                .then();
                    }
                    return chatMessageRepository.incrementRetryCount(message.getId()).then();
                });
    }

    private MessageResponse toResponse(ChatMessage m) {
        return new MessageResponse(
                m.getId(),
                m.getRoomId(),
                m.getSenderId(),
                m.getMessageType(),
                m.getMessageContent(),
                Boolean.TRUE.equals(m.getIsRead()),
                m.getCreatedAt(),
                m.getClientId());
    }
}
