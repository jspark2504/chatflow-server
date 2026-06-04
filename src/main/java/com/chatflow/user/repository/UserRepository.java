package com.chatflow.user.repository;

import com.chatflow.user.domain.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {

    Mono<User> findByEmail(String email);

    Mono<Boolean> existsByEmail(String email);

    Flux<User> findByNicknameContainingIgnoreCase(String nickname);
}
