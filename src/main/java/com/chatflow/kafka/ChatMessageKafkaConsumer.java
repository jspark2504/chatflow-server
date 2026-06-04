package com.chatflow.kafka;

import com.chatflow.kafka.dto.ChatMessageSentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatMessageKafkaConsumer {

    @KafkaListener(
            topics = ChatKafkaTopics.MESSAGE_SENT,
            groupId = "chatflow-server",
            containerFactory = "chatMessageKafkaListenerContainerFactory")
    public void onMessageSent(ChatMessageSentEvent event) {
        if (event.message() == null) {
            log.info("Kafka consumed chat.message.sent roomId={} (no message payload)", event.roomId());
            return;
        }
        log.info(
                "Kafka consumed chat.message.sent roomId={} messageId={} senderId={}",
                event.roomId(),
                event.message().messageId(),
                event.message().senderId());
    }
}
