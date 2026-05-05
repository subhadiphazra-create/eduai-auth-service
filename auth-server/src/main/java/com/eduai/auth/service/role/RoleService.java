package com.eduai.auth.service.role;

import com.eduai.auth.dto.request.role.AssignRoleRequest;
import com.eduai.auth.dto.request.role.CreateRoleRequest;
import com.eduai.auth.dto.request.role.RenameRoleRequest;
import com.eduai.auth.dto.request.role.UpdateRoleRequest;
import com.eduai.auth.dto.response.role.RoleResponse;
import com.eduai.auth.dto.response.role.UserRoleResponse;

import java.util.List;

/**
 * Role management service contract.
 *
 * <p>Authorization rules are enforced in the service layer (not just controller)
 * so they apply regardless of how the service is called (REST, Feign, internal).
 */
public interface RoleService {

    /**
     * Returns the roles of the currently authenticated user.
     * Every authenticated user can call this.
     */
    UserRoleResponse getMyRole();

    /**
     * Returns all roles defined for a given app.
     * Requires ADMIN or SUPER_ADMIN authority.
     */
    List<RoleResponse> getRolesByApp(String appId);

    /**
     * Creates a new custom role for an app.
     * Requires SUPER_ADMIN authority.
     * System roles (SUPER_ADMIN, ADMIN, CLIENT) are seeded automatically and cannot be recreated.
     */
    RoleResponse createRole(CreateRoleRequest request);

    /**
     * Updates the description of an existing role.
     * Requires SUPER_ADMIN authority.
     */
    RoleResponse updateRole(String appId, String roleName, UpdateRoleRequest request);

    /**
     * Renames a role. System roles cannot be renamed.
     * Also updates all existing user_roles entries to the new name.
     * Requires SUPER_ADMIN authority.
     */
    RoleResponse renameRole(String appId, String roleName, RenameRoleRequest request);

    /**
     * Assigns (or replaces) a user's role within an app.
     * Rules:
     * - ADMIN can promote up to ADMIN level only.
     * - SUPER_ADMIN can promote to any level.
     * - Neither can promote to a role higher than themselves.
     * - Requires ADMIN or SUPER_ADMIN authority.
     */
    UserRoleResponse assignRole(String appId, AssignRoleRequest request);

    /**
     * Seeds the three system roles (SUPER_ADMIN, ADMIN, CLIENT) for a newly registered app.
     * Called internally when an app is registered — not exposed via REST.
     */
    void seedSystemRoles(String appId, String appName);
}
