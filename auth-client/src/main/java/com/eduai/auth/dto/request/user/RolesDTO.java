package com.eduai.auth.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Represents a single role assignment.
 * Roles are stored as plain strings so every app can define its own
 * role names without touching a shared enum.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RolesDTO {

    @NotBlank(message = "Role name is required")
    private String name;
}
