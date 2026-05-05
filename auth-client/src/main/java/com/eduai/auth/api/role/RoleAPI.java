package com.eduai.auth.api.role;

import com.eduai.auth.dto.request.role.AssignRoleRequest;
import com.eduai.auth.dto.request.role.CreateRoleRequest;
import com.eduai.auth.dto.request.role.RenameRoleRequest;
import com.eduai.auth.dto.request.role.UpdateRoleRequest;
import com.eduai.auth.dto.response.role.RoleResponse;
import com.eduai.auth.dto.response.role.UserRoleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Role Management API contract.
 *
 * <p>Security summary:
 * <ul>
 *   <li>GET  /my-role             — any authenticated user</li>
 *   <li>GET  /{appId}             — ADMIN or SUPER_ADMIN of that app</li>
 *   <li>POST /                    — SUPER_ADMIN only</li>
 *   <li>PUT  /{appId}/{roleName}  — SUPER_ADMIN only</li>
 *   <li>PATCH/{appId}/{roleName}/rename — SUPER_ADMIN only</li>
 *   <li>POST /assign              — ADMIN or SUPER_ADMIN</li>
 * </ul>
 */
@Tag(name = "Role Management", description = "APIs for managing roles per application and assigning them to users")
@RequestMapping("/api/v1/roles")
public interface RoleAPI {

    @Operation(summary = "Get current authenticated user's roles")
    @ApiResponse(responseCode = "200", description = "Roles returned")
    @ApiResponse(responseCode = "401", description = "Unauthenticated")
    @GetMapping("/my-role")
    ResponseEntity<UserRoleResponse> getMyRole();

    @Operation(summary = "List all roles for a specific app — Admin or Super Admin only")
    @ApiResponse(responseCode = "200", description = "Role list returned")
    @ApiResponse(responseCode = "403", description = "Insufficient authority")
    @GetMapping("/{appId}")
    ResponseEntity<List<RoleResponse>> getRolesByApp(@PathVariable String appId);

    @Operation(summary = "Create a new role for an app — Super Admin only")
    @ApiResponse(responseCode = "201", description = "Role created")
    @ApiResponse(responseCode = "400", description = "Validation error or role already exists")
    @ApiResponse(responseCode = "403", description = "Insufficient authority")
    @PostMapping
    ResponseEntity<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request);

    @Operation(summary = "Update role description — Super Admin only")
    @ApiResponse(responseCode = "200", description = "Role updated")
    @ApiResponse(responseCode = "403", description = "Insufficient authority")
    @ApiResponse(responseCode = "404", description = "Role not found")
    @PutMapping("/{appId}/{roleName}")
    ResponseEntity<RoleResponse> updateRole(
            @PathVariable String appId,
            @PathVariable String roleName,
            @Valid @RequestBody UpdateRoleRequest request);

    @Operation(summary = "Rename a role — Super Admin only. System roles cannot be renamed.")
    @ApiResponse(responseCode = "200", description = "Role renamed")
    @ApiResponse(responseCode = "400", description = "Attempted to rename a system role")
    @ApiResponse(responseCode = "403", description = "Insufficient authority")
    @ApiResponse(responseCode = "404", description = "Role not found")
    @PatchMapping("/{appId}/{roleName}/rename")
    ResponseEntity<RoleResponse> renameRole(
            @PathVariable String appId,
            @PathVariable String roleName,
            @Valid @RequestBody RenameRoleRequest request);

    @Operation(summary = "Assign / update role for a user within an app — Admin or Super Admin only")
    @ApiResponse(responseCode = "200", description = "Role assigned")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "403", description = "Insufficient authority or attempted privilege escalation")
    @ApiResponse(responseCode = "404", description = "User or role not found")
    @PostMapping("/{appId}/assign")
    ResponseEntity<UserRoleResponse> assignRole(
            @PathVariable String appId,
            @Valid @RequestBody AssignRoleRequest request);
}
