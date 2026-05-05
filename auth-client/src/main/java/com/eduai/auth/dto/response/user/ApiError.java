package com.eduai.auth.dto.response.user;

import java.time.Instant;

public record ApiError(
        int status,
        String error,
        String message,
        String path,
        boolean authError,
        Instant timestamp
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(status, error, message, path, false, Instant.now());
    }

    public static ApiError of(int status, String error, String message, String path, boolean authError) {
        return new ApiError(status, error, message, path, authError, Instant.now());
    }
}
