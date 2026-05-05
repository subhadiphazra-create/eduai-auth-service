package com.eduai.auth.dto.request.role;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for assigning or updating a user's role within an app.
 * Only ADMIN and SUPER_ADMIN can call this. SUPER_ADMIN can set any role;
 * ADMIN cannot promote beyond ADMIN.
 */
public record AssignRoleRequest(

        @NotBlank(message = "targetUserEmail is required")
        String targetUserEmail,

        @NotBlank(message = "roleName is required")
        String roleName
) {}
