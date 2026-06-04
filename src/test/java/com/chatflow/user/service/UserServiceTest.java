package com.chatflow.user.service;

import com.chatflow.common.error.BusinessException;
import com.chatflow.infra.security.JwtService;
import com.chatflow.user.domain.User;
import com.chatflow.user.dto.LoginRequest;
import com.chatflow.user.dto.RegisterRequest;
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
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtService jwtService;

    @InjectMocks
    UserService userService;

    @Test
    void register_rejectsDuplicateUsername() {
        when(userRepository.existsByUsername("alice")).thenReturn(Mono.just(true));

        StepVerifier.create(userService.register(new RegisterRequest("alice", "password1")))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(BusinessException.class);
                    assertThat(((BusinessException) err).getStatus()).isEqualTo(HttpStatus.CONFLICT);
                })
                .verify();
    }

    @Test
    void login_returnsTokenWhenCredentialsMatch() {
        User user = User.builder().id(1L).username("alice").password("hash").build();
        when(userRepository.findByUsername("alice")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("password1", "hash")).thenReturn(true);
        when(jwtService.createToken(1L, "alice")).thenReturn("jwt-token");

        StepVerifier.create(userService.login(new LoginRequest("alice", "password1")))
                .assertNext(res -> {
                    assertThat(res.token()).isEqualTo("jwt-token");
                    assertThat(res.userId()).isEqualTo(1L);
                })
                .verifyComplete();
    }

    @Test
    void register_savesEncodedPassword() {
        when(userRepository.existsByUsername("bob")).thenReturn(Mono.just(false));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return Mono.just(u);
        });

        StepVerifier.create(userService.register(new RegisterRequest("bob", "password1")))
                .assertNext(res -> {
                    assertThat(res.id()).isEqualTo(2L);
                    assertThat(res.username()).isEqualTo("bob");
                })
                .verifyComplete();
    }
}
