package com.eduai.auth.service.impl.role;

import com.eduai.auth.dto.request.role.AssignRoleRequest;
import com.eduai.auth.dto.request.role.CreateRoleRequest;
import com.eduai.auth.dto.request.role.RenameRoleRequest;
import com.eduai.auth.dto.request.role.UpdateRoleRequest;
import com.eduai.auth.dto.response.role.RoleResponse;
import com.eduai.auth.dto.response.role.UserRoleResponse;
import com.eduai.auth.entity.role.RoleEntity;
import com.eduai.auth.entity.user.UserEntity;
import com.eduai.auth.enums.role.SystemRole;
import com.eduai.auth.exception.ResourceAlreadyExistsException;
import com.eduai.auth.exception.ResourceNotFoundException;
import com.eduai.auth.exception.RoleOperationException;
import com.eduai.auth.mapper.role.RoleMapper;
import com.eduai.auth.repository.role.RoleRepository;
import com.eduai.auth.repository.user.UserRepository;
import com.eduai.auth.security.SecurityContextHelper;
import com.eduai.auth.service.role.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository        roleRepository;
    private final UserRepository        userRepository;
    private final RoleMapper            roleMapper;
    private final SecurityContextHelper securityContextHelper;

    // ── Get current user's roles ──────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserRoleResponse getMyRole() {
        UserEntity user = securityContextHelper.getCurrentUser();
        return buildUserRoleResponse(user);
    }

    // ── List roles by app ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getRolesByApp(String appId) {
        return roleRepository.findAllByAppId(appId)
                .stream()
                .map(roleMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ── Create role ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        String roleName = request.roleName().toUpperCase();

        // Prevent duplicating system roles
        if (isSystemRole(roleName)) {
            throw new RoleOperationException(
                "'" + roleName + "' is a system role and is seeded automatically. You cannot create it manually.");
        }

        if (roleRepository.existsByAppIdAndRoleName(request.appId(), roleName)) {
            throw new ResourceAlreadyExistsException(
                "Role '" + roleName + "' already exists in app '" + request.appId() + "'.");
        }

        RoleEntity entity = roleMapper.toEntity(request);
        entity.setRoleName(roleName);
        RoleEntity saved = roleRepository.save(entity);
        log.info("Created role '{}' for app '{}'", roleName, request.appId());
        return roleMapper.toResponse(saved);
    }

    // ── Update description ────────────────────────────────────────────────────

    @Override
    @Transactional
    public RoleResponse updateRole(String appId, String roleName, UpdateRoleRequest request) {
        RoleEntity role = findRole(appId, roleName);
        role.setDescription(request.description());
        return roleMapper.toResponse(roleRepository.save(role));
    }

    // ── Rename role ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public RoleResponse renameRole(String appId, String roleName, RenameRoleRequest request) {
        RoleEntity role = findRole(appId, roleName);

        if (role.isSystemRole()) {
            throw new RoleOperationException(
                "System role '" + roleName + "' cannot be renamed.");
        }

        String newName = request.newRoleName().toUpperCase();
        if (roleRepository.existsByAppIdAndRoleName(appId, newName)) {
            throw new ResourceAlreadyExistsException(
                "Role '" + newName + "' already exists in app '" + appId + "'.");
        }

        String oldName = role.getRoleName();
        role.setRoleName(newName);
        RoleEntity saved = roleRepository.save(role);

        // Update all users who held the old role name
        int updated = roleRepository.bulkRenameUserRole(appId, oldName, newName);
        log.info("Renamed role '{}' → '{}' in app '{}'. Updated {} user records.", oldName, newName, appId, updated);

        return roleMapper.toResponse(saved);
    }

    // ── Assign role to user ───────────────────────────────────────────────────

    @Override
    @Transactional
    public UserRoleResponse assignRole(String appId, AssignRoleRequest request) {
        UserEntity caller = securityContextHelper.getCurrentUser();
        String targetEmail = request.targetUserEmail();
        String newRoleName = request.roleName().toUpperCase();

        // Validate that the target role exists in this app
        if (!roleRepository.existsByAppIdAndRoleName(appId, newRoleName)) {
            throw new ResourceNotFoundException(
                "Role '" + newRoleName + "' does not exist in app '" + appId + "'. Create it first.");
        }

        // Load target user
        UserEntity target = userRepository.findByEmailAndAppIdAndDeletedAtIsNull(targetEmail, appId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "User '" + targetEmail + "' not found in app '" + appId + "'."));

        // Authorization: prevent self-promotion
        if (caller.getId().equals(target.getId())) {
            throw new AccessDeniedException("You cannot change your own role.");
        }

        // Authorization: enforce privilege hierarchy
        SystemRole callerHighest = SecurityContextHelper.resolveHighestRole(caller);
        SystemRole targetCurrent = SecurityContextHelper.resolveHighestRole(target);
        SystemRole targetNew     = resolveAsSystemRole(newRoleName);

        // Can the caller manage the target's current role?
        if (!callerHighest.canManage(targetCurrent)) {
            throw new AccessDeniedException(
                "You cannot modify the role of a user with equal or higher privilege than yours.");
        }

        // Can the caller grant the new role?
        if (targetNew != null && !callerHighest.canManage(targetNew)) {
            throw new AccessDeniedException(
                "You cannot grant a role equal to or higher than your own.");
        }

        // Replace all roles with the single new role (one user = one role at a time)
        target.setRoles(Set.of(newRoleName));
        UserEntity saved = userRepository.save(target);

        log.info("User '{}' assigned role '{}' to '{}' in app '{}'",
                caller.getEmail(), newRoleName, targetEmail, appId);

        return buildUserRoleResponse(saved);
    }

    // ── Seed system roles ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public void seedSystemRoles(String appId, String appName) {
        Arrays.stream(SystemRole.values()).forEach(sr -> {
            if (!roleRepository.existsByAppIdAndRoleName(appId, sr.value())) {
                roleRepository.save(RoleEntity.builder()
                        .appId(appId)
                        .roleName(sr.value())
                        .description("System role for " + appName + ": " + sr.value())
                        .systemRole(true)
                        .build());
            }
        });
        log.info("Seeded system roles for app '{}'", appId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private RoleEntity findRole(String appId, String roleName) {
        return roleRepository.findByAppIdAndRoleName(appId, roleName.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Role '" + roleName + "' not found in app '" + appId + "'."));
    }

    private boolean isSystemRole(String roleName) {
        return Arrays.stream(SystemRole.values())
                .anyMatch(sr -> sr.value().equals(roleName));
    }

    /**
     * If the given role name matches a SystemRole, return it; else return null.
     * Used to check privilege levels when assigning roles.
     */
    private SystemRole resolveAsSystemRole(String roleName) {
        return Arrays.stream(SystemRole.values())
                .filter(sr -> sr.value().equals(roleName))
                .findFirst()
                .orElse(null); // Custom roles have no system privilege level — always grantable by ADMIN+
    }

    private UserRoleResponse buildUserRoleResponse(UserEntity user) {
        return UserRoleResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .appId(user.getAppId())
                .roles(user.getRoles() != null ? Set.copyOf(user.getRoles()) : Set.of())
                .highestRole(SecurityContextHelper.resolveHighestRoleName(user))
                .build();
    }
}
