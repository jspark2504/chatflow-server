package com.chatflow.kafka;

import com.chatflow.chat.dto.MessageResponse;
import com.chatflow.kafka.dto.ChatMessageSentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageKafkaPublisher {

    private final KafkaTemplate<String, ChatMessageSentEvent> kafkaTemplate;

    public Mono<Void> publish(long roomId, MessageResponse message) {
        ChatMessageSentEvent event = new ChatMessageSentEvent(roomId, message, Instant.now());
        return Mono.fromFuture(() -> kafkaTemplate.send(ChatKafkaTopics.MESSAGE_SENT, String.valueOf(roomId), event))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                        .maxBackoff(Duration.ofSeconds(1)))
                .doOnError(e -> log.error("Kafka publish failed after retries roomId={} messageId={}",
                        roomId, message.messageId(), e))
                .onErrorComplete()
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
