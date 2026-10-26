package org.demo.backend.global.exception.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@JsonPropertyOrder({"status", "code", "message", "timestamp"})
public class ErrorDetail<T> {

    private final int status;
    private final String code;
    private final T message;
    private final LocalDateTime timestamp;

    public ErrorDetail(HttpStatus status, T message) {
        this(status, status.name(), message);
    }

    public ErrorDetail(HttpStatus status, String code, T message) {
        this.status = status.value();
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
