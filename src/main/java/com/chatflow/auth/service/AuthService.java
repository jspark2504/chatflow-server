package com.chatflow.auth.service;

import com.chatflow.auth.dto.LoginRequest;
import com.chatflow.auth.dto.LoginResponse;
import com.chatflow.auth.dto.RegisterRequest;
import com.chatflow.common.error.BusinessException;
import com.chatflow.infra.security.JwtService;
import com.chatflow.user.domain.User;
import com.chatflow.user.dto.UserResponse;
import com.chatflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public Mono<UserResponse> signup(RegisterRequest request) {
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
                .map(this::toUserResponse);
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

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}
