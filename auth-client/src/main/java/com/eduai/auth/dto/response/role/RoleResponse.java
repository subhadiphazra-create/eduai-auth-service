package com.eduai.auth.dto.response.role;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for a role record.
 */
@Getter
@Setter
@Builder
public class RoleResponse {

    private UUID    id;
    private String  appId;
    private String  roleName;
    private String  description;
    private boolean systemRole;   // true if this is a built-in system role
    private Instant createdAt;
    private Instant updatedAt;
}
