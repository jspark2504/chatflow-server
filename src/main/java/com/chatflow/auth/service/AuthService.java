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
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
                    return Mono.fromCallable(() -> User.builder()
                                    .email(request.email())
                                    .password(passwordEncoder.encode(request.password()))
                                    .nickname(request.nickname())
                                    .onlineStatus(false)
                                    .build())
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .flatMap(userRepository::save)
                .flatMap(saved -> saved.getId() != null
                        ? Mono.just(saved)
                        : userRepository.findByEmail(saved.getEmail()))
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Signup failed")))
                .map(this::toUserResponse)
                .onErrorMap(DuplicateKeyException.class,
                        e -> new BusinessException(HttpStatus.CONFLICT, "Email already registered"))
                .onErrorMap(DataAccessException.class,
                        e -> new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error during signup"));
    }

    public Mono<LoginResponse> login(LoginRequest request) {
        return userRepository.findByEmail(request.email())
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid credentials")))
                .flatMap(u -> Mono.fromCallable(() -> passwordEncoder.matches(request.password(), u.getPassword()))
                        .subscribeOn(Schedulers.boundedElastic())
                        .filter(Boolean::booleanValue)
                        .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid credentials")))
                        .thenReturn(u))
                .map(u -> new LoginResponse(
                        u.getId(),
                        u.getEmail(),
                        u.getNickname(),
                        jwtService.createToken(u.getId(), u.getEmail(), u.getNickname())));
    }

    private UserResponse toUserResponse(User user) {
        if (user.getId() == null) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "User id missing after save");
        }
        return new UserResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}
