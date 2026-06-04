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
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public Mono<UserResponse> register(RegisterRequest request) {
        return userRepository.existsByUsername(request.username())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new BusinessException(HttpStatus.CONFLICT, "Username already taken"));
                    }
                    User user = User.builder()
                            .username(request.username())
                            .password(passwordEncoder.encode(request.password()))
                            .build();
                    return userRepository.save(user);
                })
                .map(u -> new UserResponse(u.getId(), u.getUsername()));
    }

    public Mono<LoginResponse> login(LoginRequest request) {
        return userRepository.findByUsername(request.username())
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid credentials")))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid credentials")))
                .map(u -> new LoginResponse(
                        jwtService.createToken(u.getId(), u.getUsername()),
                        u.getId(),
                        u.getUsername()));
    }

    public Mono<UserResponse> me(long userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new BusinessException(HttpStatus.NOT_FOUND, "User not found")))
                .map(u -> new UserResponse(u.getId(), u.getUsername()));
    }
}
