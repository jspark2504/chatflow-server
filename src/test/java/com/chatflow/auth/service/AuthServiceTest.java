package com.chatflow.auth.service;

import com.chatflow.auth.dto.LoginRequest;
import com.chatflow.auth.dto.RegisterRequest;
import com.chatflow.common.error.BusinessException;
import com.chatflow.infra.security.JwtService;
import com.chatflow.user.domain.User;
import com.chatflow.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtService jwtService;

    @InjectMocks
    AuthService authService;

    @Test
    void signup_rejectsDuplicateEmail() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(Mono.just(true));

        StepVerifier.create(authService.signup(
                        new RegisterRequest("test@test.com", "password1", "홍길동")))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(BusinessException.class);
                    assertThat(((BusinessException) err).getStatus()).isEqualTo(HttpStatus.CONFLICT);
                })
                .verify();
    }

    @Test
    void login_returnsTokenWhenCredentialsMatch() {
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .nickname("홍길동")
                .password("hash")
                .build();
        when(userRepository.findByEmail("test@test.com")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("password1", "hash")).thenReturn(true);
        when(jwtService.createToken(1L, "test@test.com", "홍길동")).thenReturn("jwt-token");

        StepVerifier.create(authService.login(new LoginRequest("test@test.com", "password1")))
                .assertNext(res -> {
                    assertThat(res.accessToken()).isEqualTo("jwt-token");
                    assertThat(res.userId()).isEqualTo(1L);
                })
                .verifyComplete();
    }

    @Test
    void signup_savesEncodedPassword() {
        when(userRepository.existsByEmail("bob@test.com")).thenReturn(Mono.just(false));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return Mono.just(u);
        });

        StepVerifier.create(authService.signup(
                        new RegisterRequest("bob@test.com", "password1", "bob")))
                .assertNext(res -> {
                    assertThat(res.userId()).isEqualTo(2L);
                    assertThat(res.email()).isEqualTo("bob@test.com");
                })
                .verifyComplete();
    }
}
