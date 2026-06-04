package com.chatflow.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Mono<ResponseEntity<ApiError>> handleBusiness(BusinessException ex, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(ex.getStatus()).body(toError(exchange, ex.getStatus(), ex.getMessage())));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiError>> handleValidation(WebExchangeBindException ex, ServerWebExchange exchange) {
        String msg = ex.getBindingResult().getAllErrors().isEmpty()
                ? "Validation failed"
                : ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return Mono.just(ResponseEntity.badRequest().body(toError(exchange, HttpStatus.BAD_REQUEST, msg)));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiError>> handleGeneric(Exception ex, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(toError(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error")));
    }

    private static ApiError toError(ServerWebExchange exchange, HttpStatus status, String message) {
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message,
                exchange.getRequest().getPath().value());
    }
}
