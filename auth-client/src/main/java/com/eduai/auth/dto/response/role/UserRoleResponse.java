package com.eduai.auth.dto.response.role;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

/**
 * Response DTO showing the currently authenticated user's roles within their app.
 */
@Getter
@Setter
@Builder
public class UserRoleResponse {

    private UUID        userId;
    private String      email;
    private String      appId;
    private Set<String> roles;
    private String      highestRole;  // Convenience field: the highest-privilege role
}
