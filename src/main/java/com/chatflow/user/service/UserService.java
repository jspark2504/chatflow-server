package com.chatflow.user.service;

import com.chatflow.common.error.BusinessException;
import com.chatflow.infra.security.JwtService;
import com.chatflow.user.domain.User;
import com.chatflow.user.dto.LoginRequest;
import com.chatflow.user.dto.LoginResponse;
import com.chatflow.user.dto.RegisterRequest;
import com.chatflow.user.dto.UserResponse;
import com.chatflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int SEARCH_LIMIT = 20;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public Mono<UserResponse> register(RegisterRequest request) {
        return userRepository.existsByEmail(request.email())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new BusinessException(HttpStatus.CONFLICT, "Email already registered"));
                    }
                    Instant now = Instant.now();
                    User user = User.builder()
                            .email(request.email())
                            .password(passwordEncoder.encode(request.password()))
                            .nickname(request.nickname())
                            .onlineStatus(false)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    return userRepository.save(user);
                })
                .map(this::toResponse);
    }

    public Mono<LoginResponse> login(LoginRequest request) {
        return userRepository.findByEmail(request.email())
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid credentials")))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid credentials")))
                .map(u -> new LoginResponse(
                        u.getId(),
                        u.getEmail(),
                        u.getNickname(),
                        jwtService.createToken(u.getId(), u.getEmail(), u.getNickname())));
    }

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
        return new UserResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}
