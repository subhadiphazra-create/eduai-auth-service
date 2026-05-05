package com.eduai.auth.dto.request.app;


import jakarta.validation.constraints.NotBlank;

public record AppRegistrationRequest(
        @NotBlank String appId,
        @NotBlank String appName,
        String displayName,
        String supportEmail,
        String oauthSuccessRedirectUrl,
        String oauthFailureRedirectUrl
) {}