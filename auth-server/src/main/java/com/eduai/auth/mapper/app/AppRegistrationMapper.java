package com.eduai.auth.mapper.app;

import com.eduai.auth.dto.request.app.AppRegistrationRequest;
import com.eduai.auth.dto.response.app.AppRegistrationResponse;
import com.eduai.auth.entity.app.AppRegistration;
import com.eduai.auth.mapper.GenericMapper;
import org.springframework.stereotype.Component;

/**
 * Maps between AppRegistration entity and its DTOs.
 * Fixed: the original controller was returning raw entity objects instead of response DTOs.
 */
@Component
public class AppRegistrationMapper implements GenericMapper<AppRegistration, AppRegistrationRequest, AppRegistrationResponse> {

    @Override
    public AppRegistration toEntity(AppRegistrationRequest request) {
        if (request == null) return null;
        return AppRegistration.builder()
                .appId(request.appId())
                .appName(request.appName())
                .displayName(request.displayName())
                .supportEmail(request.supportEmail())
                .oauthSuccessRedirectUrl(request.oauthSuccessRedirectUrl())
                .oauthFailureRedirectUrl(request.oauthFailureRedirectUrl())
                .active(true)
                .build();
    }

    @Override
    public AppRegistrationResponse toResponse(AppRegistration entity) {
        if (entity == null) return null;
        return AppRegistrationResponse.builder()
                .id(entity.getId())
                .appId(entity.getAppId())
                .appName(entity.getAppName())
                .displayName(entity.getDisplayName())
                .supportEmail(entity.getSupportEmail())
                .oauthSuccessRedirectUrl(entity.getOauthSuccessRedirectUrl())
                .oauthFailureRedirectUrl(entity.getOauthFailureRedirectUrl())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
