package com.eduai.auth.dto.request.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating a new role within an app.
 */
public record CreateRoleRequest(

        @NotBlank(message = "appId is required")
        String appId,

        /**
         * Role name must be uppercase letters/underscores only, e.g. "TEACHER", "HR_MANAGER".
         * This ensures consistency and avoids typo-class ambiguities.
         */
        @NotBlank(message = "roleName is required")
        @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,49}$",
                 message = "roleName must be UPPER_SNAKE_CASE, 2-50 chars (e.g. TEACHER, HR_MANAGER)")
        String roleName,

        @Size(max = 500, message = "description must not exceed 500 characters")
        String description
) {}
