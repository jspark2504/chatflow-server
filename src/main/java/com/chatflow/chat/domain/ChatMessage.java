package com.chatflow.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("chat_message")
public class ChatMessage {

    @Id
    private Long id;

    @Column("room_id")
    private Long roomId;

    @Column("sender_id")
    private Long senderId;

    @Column("message_type")
    private String messageType;

    @Column("message_content")
    private String messageContent;

    @Column("is_read")
    private Boolean isRead;

    @Column("created_at")
    private Instant createdAt;

    @Column("client_id")
    private String clientId;

    @Column("published_at")
    private Instant publishedAt;

    @Column("retry_count")
    private int retryCount;

    @Column("publish_failed_at")
    private Instant publishFailedAt;
}
