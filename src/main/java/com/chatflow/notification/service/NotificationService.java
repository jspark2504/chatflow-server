package com.chatflow.notification.service;

import com.chatflow.chat.repository.ChatRoomMemberRepository;
import com.chatflow.kafka.dto.ChatMessageSentEvent;
import com.chatflow.notification.repository.NotificationRepository;
import com.chatflow.redis.RedisPresenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final RedisPresenceRepository presenceRepository;
    private final NotificationRepository notificationRepository;

    /**
     * Kafka 이벤트로부터 오프라인 수신자를 식별하고 알림을 DB에 기록합니다.
     *
     * 흐름:
     *   방 멤버 조회 → 발신자 제외 → Redis로 온라인 여부 확인 → 오프라인 대상만 INSERT IGNORE
     *
     * Redis 장애 시: 전원 오프라인으로 간주하여 알림을 모두 저장 (누락 방지 우선).
     */
    public Mono<Void> processOfflineNotifications(ChatMessageSentEvent event) {
        long roomId    = event.roomId();
        long senderId  = event.message().senderId();
        long messageId = event.message().messageId();

        return chatRoomMemberRepository.findByRoomId(roomId)
                .map(member -> member.getUserId())
                .filter(uid -> uid != senderId)
                .collect(Collectors.toSet())
                .flatMap(memberIds -> resolveOfflineIds(memberIds)
                        .map(offlineIds -> {
                            log.debug("[notify] roomId={} messageId={} offlineReceivers={}",
                                    roomId, messageId, offlineIds);
                            return offlineIds;
                        }))
                .flatMapMany(Flux::fromIterable)
                .flatMap(receiverId -> notificationRepository.insertIgnore(
                        receiverId, senderId, roomId, messageId, event.message().createdAt()))
                .then();
    }

    /**
     * candidates 중 오프라인인 userId Set을 반환합니다.
     * Redis 장애 시 전원 오프라인으로 처리 (알림 누락 방지 우선).
     */
    private Mono<Set<Long>> resolveOfflineIds(Set<Long> candidates) {
        if (candidates.isEmpty()) {
            return Mono.just(Collections.emptySet());
        }
        return presenceRepository.getOnlineUserIds(candidates)
                .map(onlineIds -> {
                    Set<Long> offlineIds = new HashSet<>(candidates);
                    offlineIds.removeAll(onlineIds);
                    return offlineIds;
                })
                .onErrorResume(ex -> {
                    log.warn("[notify] Redis presence check failed, treating all as offline: {}", ex.getMessage());
                    return Mono.just(new HashSet<>(candidates));
                });
    }
}
