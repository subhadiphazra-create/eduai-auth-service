package com.eduai.auth.service.role;

import com.eduai.auth.entity.user.UserEntity;
import com.eduai.auth.enums.role.SystemRole;
import com.eduai.auth.exception.ResourceNotFoundException;
import com.eduai.auth.repository.role.RoleRepository;
import com.eduai.auth.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Centralized multi-role access control service.
 *
 * <p>This service is the single source of truth for authorization decisions.
 * Other internal services (and potentially external services via Feign) should call
 * this service rather than implementing their own role checks.
 *
 * <p>Design:
 * <ul>
 *   <li>All checks are app-scoped — a user's roles in app "eduai" don't affect "hrms".</li>
 *   <li>The service operates on userId (UUID) for security — never on email strings alone.</li>
 *   <li>Results are intentionally boolean so callers can't distinguish between "no role"
 *       and "user not found" for security reasons.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccessControlService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Returns true if the user identified by {@code userId} has ALL of the specified roles
     * within the given app.
     *
     * @param userId UUID of the user
     * @param appId  Application scope for the check
     * @param roles  One or more role names to verify
     */
    @Transactional(readOnly = true)
    public boolean hasAllRoles(UUID userId, String appId, String... roles) {
        return findUser(userId, appId)
                .map(user -> {
                    Set<String> userRoles = user.getRoles();
                    for (String role : roles) {
                        if (!userRoles.contains(role)) return false;
                    }
                    return true;
                })
                .orElse(false);
    }

    /**
     * Returns true if the user has ANY of the specified roles within the given app.
     *
     * @param userId UUID of the user
     * @param appId  Application scope for the check
     * @param roles  One or more role names — at least one must match
     */
    @Transactional(readOnly = true)
    public boolean hasAnyRole(UUID userId, String appId, String... roles) {
        return findUser(userId, appId)
                .map(user -> {
                    Set<String> userRoles = user.getRoles();
                    for (String role : roles) {
                        if (userRoles.contains(role)) return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Returns true if the user is a SUPER_ADMIN within the given app.
     */
    @Transactional(readOnly = true)
    public boolean isSuperAdmin(UUID userId, String appId) {
        return hasAnyRole(userId, appId, SystemRole.SUPER_ADMIN.value());
    }

    /**
     * Returns true if the user is an ADMIN or SUPER_ADMIN within the given app.
     */
    @Transactional(readOnly = true)
    public boolean isAdminOrAbove(UUID userId, String appId) {
        return hasAnyRole(userId, appId, SystemRole.ADMIN.value(), SystemRole.SUPER_ADMIN.value());
    }

    /**
     * Returns true if the user has at least CLIENT-level access (i.e., is an active, verified user).
     * This is the most basic authorization check.
     */
    @Transactional(readOnly = true)
    public boolean isActiveUser(UUID userId, String appId) {
        return findUser(userId, appId)
                .map(u -> u.isEnabled() && Boolean.TRUE.equals(u.getEmailVerified()))
                .orElse(false);
    }

    /**
     * Returns the roles of a user within a given app.
     * Returns an empty set if the user is not found or inactive.
     */
    @Transactional(readOnly = true)
    public Set<String> getUserRoles(UUID userId, String appId) {
        return findUser(userId, appId)
                .map(UserEntity::getRoles)
                .orElse(Set.of());
    }

    /**
     * Returns the highest SystemRole of the user in the given app, or empty if they have none.
     */
    @Transactional(readOnly = true)
    public Optional<SystemRole> getHighestSystemRole(UUID userId, String appId) {
        return findUser(userId, appId).map(user -> {
            Set<String> roles = user.getRoles();
            if (roles.contains(SystemRole.SUPER_ADMIN.value())) return SystemRole.SUPER_ADMIN;
            if (roles.contains(SystemRole.ADMIN.value()))       return SystemRole.ADMIN;
            if (roles.contains(SystemRole.CLIENT.value()))      return SystemRole.CLIENT;
            return null;
        });
    }

    /**
     * Returns true if a role name is valid and defined within the given app.
     * Other services can call this to validate role names before using them.
     */
    @Transactional(readOnly = true)
    public boolean isValidRoleInApp(String appId, String roleName) {
        return roleRepository.existsByAppIdAndRoleName(appId, roleName);
    }

    // ── Internal helper ───────────────────────────────────────────────────────

    /**
     * Loads a user by UUID scoped to an app.
     * Returns empty rather than throwing, so callers can treat "user not found" as "no access".
     */
    private Optional<UserEntity> findUser(UUID userId, String appId) {
        return userRepository.findById(userId)
                .filter(u -> u.getAppId().equals(appId) && u.getDeletedAt() == null);
    }
}
