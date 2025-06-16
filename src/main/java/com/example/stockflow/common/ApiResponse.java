package com.example.stockflow.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ApiResponse<T> {
    private String path;
    private String status;
    private T data;
    private LocalDateTime timestamp;


    private ApiResponse(String path, String status, T data) {
        this.path = path;
        this.status = status;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(String path, T data) {
        return new ApiResponse<>(path, Status.SUCCESS.toString(), data);
    }

    public static ApiResponse<Error> error(String path, String message) {
        return new ApiResponse<>(path, Status.ERROR.toString(), new Error(message));
    }

    @Getter
    @AllArgsConstructor
    public static class Success<T> {
    }

    @Getter
    @AllArgsConstructor
    public static class Error {
        private String message;
    }

    public enum Status {
        SUCCESS,
        ERROR
    }
}
