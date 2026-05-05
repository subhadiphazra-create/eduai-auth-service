package com.eduai.auth.entity.role;

import com.eduai.auth.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a role available within a specific application.
 *
 * <p>Design decisions:
 * <ul>
 *   <li>Roles are scoped per app (appId + roleName must be unique together).</li>
 *   <li>System roles (SUPER_ADMIN, ADMIN, CLIENT) are seeded automatically when an app is registered
 *       and cannot be renamed or deleted.</li>
 *   <li>Apps can create additional custom roles (TEACHER, HR_MANAGER, etc.).</li>
 *   <li>Role names are stored in UPPER_SNAKE_CASE for consistency.</li>
 * </ul>
 */
@Entity
@Table(
    name = "roles",
    indexes = {
        @Index(name = "role_app_name_idx", columnList = "appId, roleName", unique = true)
    }
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleEntity extends BaseEntity {

    /**
     * The app this role belongs to — mirrors AppRegistration.appId.
     * Stored as a plain string for fast lookup without a join.
     */
    @Column(nullable = false, updatable = false)
    private String appId;

    /**
     * Role name in UPPER_SNAKE_CASE (e.g. TEACHER, HR_MANAGER).
     * System roles: SUPER_ADMIN, ADMIN, CLIENT.
     */
    @Column(nullable = false)
    private String roleName;

    /** Optional human-readable description for this role. */
    @Column(length = 500)
    private String description;

    /**
     * Whether this is a system-defined role.
     * System roles cannot be renamed or deleted — only custom roles can.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean systemRole = false;
}
