package com.chatflow.user.service;

import com.chatflow.common.error.BusinessException;
import com.chatflow.user.domain.User;
import com.chatflow.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Test
    void findById_returnsUser() {
        User user = User.builder().id(1L).email("a@test.com").nickname("alice").build();
        when(userRepository.findById(1L)).thenReturn(Mono.just(user));

        StepVerifier.create(userService.findById(1L))
                .assertNext(res -> {
                    assertThat(res.userId()).isEqualTo(1L);
                    assertThat(res.nickname()).isEqualTo("alice");
                })
                .verifyComplete();
    }

    @Test
    void findById_notFound() {
        when(userRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.findById(99L))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(BusinessException.class);
                    assertThat(((BusinessException) err).getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                })
                .verify();
    }
}
