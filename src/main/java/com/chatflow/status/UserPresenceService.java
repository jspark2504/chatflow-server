package com.chatflow.status;

import com.chatflow.redis.PresenceRedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserPresenceService {

    private final PresenceRedisPublisher presenceRedisPublisher;

    public Mono<Void> broadcastOnline(long userId) {
        return presenceRedisPublisher.publish(userId, true);
    }

    public Mono<Void> broadcastOffline(long userId) {
        return presenceRedisPublisher.publish(userId, false);
    }
}
