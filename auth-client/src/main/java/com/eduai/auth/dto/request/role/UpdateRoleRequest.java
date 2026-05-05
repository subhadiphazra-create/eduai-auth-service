package com.eduai.auth.dto.request.role;

import jakarta.validation.constraints.Size;

/**
 * Request body for updating a role's description.
 * Role name change is separate (RenameRoleRequest) to make intent clear.
 */
public record UpdateRoleRequest(

        @Size(max = 500, message = "description must not exceed 500 characters")
        String description
) {}
