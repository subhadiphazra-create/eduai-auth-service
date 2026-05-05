package com.eduai.auth.controller.role;

import com.eduai.auth.api.role.RoleAPI;
import com.eduai.auth.dto.request.role.AssignRoleRequest;
import com.eduai.auth.dto.request.role.CreateRoleRequest;
import com.eduai.auth.dto.request.role.RenameRoleRequest;
import com.eduai.auth.dto.request.role.UpdateRoleRequest;
import com.eduai.auth.dto.response.role.RoleResponse;
import com.eduai.auth.dto.response.role.UserRoleResponse;
import com.eduai.auth.service.role.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for role management.
 *
 * <p>All business logic and authorization checks live in {@link RoleService}.
 * This controller is intentionally thin — it only handles HTTP concerns
 * (status codes, request parsing, response wrapping).
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class RoleController implements RoleAPI {

    private final RoleService roleService;

    @Override
    public ResponseEntity<UserRoleResponse> getMyRole() {
        return ResponseEntity.ok(roleService.getMyRole());
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<List<RoleResponse>> getRolesByApp(String appId) {
        return ResponseEntity.ok(roleService.getRolesByApp(appId));
    }

    @Override
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<RoleResponse> createRole(@Valid CreateRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(request));
    }

    @Override
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<RoleResponse> updateRole(String appId, String roleName,
                                                    @Valid UpdateRoleRequest request) {
        return ResponseEntity.ok(roleService.updateRole(appId, roleName, request));
    }

    @Override
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<RoleResponse> renameRole(String appId, String roleName,
                                                    @Valid RenameRoleRequest request) {
        return ResponseEntity.ok(roleService.renameRole(appId, roleName, request));
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<UserRoleResponse> assignRole(String appId,
                                                        @Valid AssignRoleRequest request) {
        return ResponseEntity.ok(roleService.assignRole(appId, request));
    }
}
