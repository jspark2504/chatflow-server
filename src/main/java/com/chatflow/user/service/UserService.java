package com.chatflow.user.service;

import com.chatflow.common.error.BusinessException;
import com.chatflow.user.domain.User;
import com.chatflow.user.dto.UserResponse;
import com.chatflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int SEARCH_LIMIT = 20;

    private final UserRepository userRepository;

    public Mono<UserResponse> findById(long userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.NOT_FOUND, "User not found")))
                .map(this::toResponse);
    }

    public Flux<UserResponse> searchByNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return Flux.error(new BusinessException(HttpStatus.BAD_REQUEST, "nickname is required"));
        }
        return userRepository.findByNicknameContainingIgnoreCase(nickname.trim())
                .take(SEARCH_LIMIT)
                .map(this::toResponse);
    }

    public Mono<UserResponse> me(long userId) {
        return findById(userId);
    }

    private UserResponse toResponse(User user) {
        if (user.getId() == null) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "User id missing");
        }
        return new UserResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}
