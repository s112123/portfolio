package org.demo.backend.global.api.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonPropertyOrder({"success", "data", "timestamp"})
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final LocalDateTime timestamp;

    private ApiResponse(boolean success, T data) {
        this.success = success;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data);
    }

    public static ApiResponse<Void> failure() {
        return new ApiResponse<>(false, null);
    }

    public static <T> ApiResponse<T> failure(T data) {
        return new ApiResponse<>(false, data);
    }
}
