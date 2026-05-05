package com.eduai.auth.dto.request.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for renaming an existing role within an app.
 * System roles (SUPER_ADMIN, ADMIN, CLIENT) cannot be renamed.
 */
public record RenameRoleRequest(

        @NotBlank(message = "newRoleName is required")
        @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,49}$",
                 message = "newRoleName must be UPPER_SNAKE_CASE, 2-50 chars")
        String newRoleName
) {}
