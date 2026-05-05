package com.eduai.auth.dto.request.token;

import com.eduai.auth.dto.response.user.UserResponseDTO;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType,
        UserResponseDTO user
) {
    public static TokenResponse of(String accessToken, String refreshToken, long expiresIn, UserResponseDTO user) {
        return new TokenResponse(accessToken, refreshToken, expiresIn, "Bearer", user);
    }
}
