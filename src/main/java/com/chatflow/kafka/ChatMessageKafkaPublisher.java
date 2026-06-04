package com.chatflow.kafka;

import com.chatflow.chat.dto.MessageResponse;
import com.chatflow.kafka.dto.ChatMessageSentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageKafkaPublisher {

    private final KafkaTemplate<String, ChatMessageSentEvent> kafkaTemplate;

    public Mono<Void> publish(long roomId, MessageResponse message) {
        return Mono.fromRunnable(() -> {
                    ChatMessageSentEvent event = new ChatMessageSentEvent(roomId, message, Instant.now());
                    kafkaTemplate.send(ChatKafkaTopics.MESSAGE_SENT, String.valueOf(roomId), event)
                            .whenComplete((result, ex) -> {
                                if (ex != null) {
                                    log.warn("Kafka publish failed roomId={}: {}", roomId, ex.getMessage());
                                }
                            });
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
