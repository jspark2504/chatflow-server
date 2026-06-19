package com.chatflow.kafka;

import com.chatflow.kafka.dto.ChatMessageSentEvent;
import com.chatflow.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageKafkaConsumer {

    private final NotificationService notificationService;

    /**
     * Kafka consumer thread (Spring Kafka internal SimpleAsyncTaskExecutor) 위에서 실행됨.
     * WebFlux Netty event loop와 완전히 독립된 스레드이므로 block()이 안전.
     */
    @KafkaListener(
            topics = ChatKafkaTopics.MESSAGE_SENT,
            groupId = "chatflow-server",
            containerFactory = "chatMessageKafkaListenerContainerFactory")
    public void onMessageSent(ChatMessageSentEvent event) {
        if (event.message() == null) {
            log.debug("Kafka consumed chat.message.sent roomId={} (no message payload)", event.roomId());
            return;
        }
        log.debug("Kafka consumed chat.message.sent roomId={} messageId={} senderId={}",
                event.roomId(),
                event.message().messageId(),
                event.message().senderId());

        notificationService.processOfflineNotifications(event)
                .block(Duration.ofSeconds(10));
    }
}
