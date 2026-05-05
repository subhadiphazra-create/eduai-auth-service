package com.eduai.auth.dto.response.app;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for AppRegistration.
 * Fixed: original was missing @Getter making fields inaccessible for serialization.
 */
@Getter
@Setter
@Builder
public class AppRegistrationResponse {

    private UUID    id;
    private String  appId;
    private String  appName;
    private String  displayName;
    private String  supportEmail;
    private String  oauthSuccessRedirectUrl;
    private String  oauthFailureRedirectUrl;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
