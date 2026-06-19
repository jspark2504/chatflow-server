package com.chatflow.notification.domain;

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
@Table("notifications")
public class Notification {

    @Id
    private Long id;

    @Column("receiver_id")
    private Long receiverId;

    @Column("sender_id")
    private Long senderId;

    @Column("room_id")
    private Long roomId;

    @Column("message_id")
    private Long messageId;

    @Column("is_read")
    private Boolean isRead;

    @Column("created_at")
    private Instant createdAt;
}
