package com.chatflow.user.repository;

import com.chatflow.user.domain.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {

    Mono<User> findByUsername(String username);

    Mono<Boolean> existsByUsername(String username);
}
