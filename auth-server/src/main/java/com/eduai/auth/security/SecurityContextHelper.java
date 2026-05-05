package com.eduai.auth.security;

import com.eduai.auth.entity.user.UserEntity;
import com.eduai.auth.enums.role.SystemRole;
import com.eduai.auth.exception.ResourceNotFoundException;
import com.eduai.auth.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Centralized utility for extracting information from the Spring Security context.
 *
 * <p>This replaces the scattered, bug-prone context lookups in the original UserServiceImpl
 * (which did a full table scan to resolve appId). We now store userId in the JWT subject,
 * making lookups O(1) by primary key.
 */
@Component
@RequiredArgsConstructor
public class SecurityContextHelper {

    private final UserRepository userRepository;

    /**
     * Returns the currently authenticated user's email (set as principal in JwtAuthenticationFilter).
     */
    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user in context.");
        }
        return auth.getName();
    }

    /**
     * Returns the full UserEntity of the currently authenticated user.
     * Loads by email — always returns a live entity (not a cached/stale one).
     */
    public UserEntity getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + email));
    }

    /**
     * Returns all authority strings (role names) for the current user from the security context.
     * This is cheaper than a DB call — authorities are decoded from the JWT.
     */
    public Set<String> getCurrentUserRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return Set.of();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    /**
     * Returns true if the current user has the specified authority/role.
     */
    public boolean hasRole(String roleName) {
        return getCurrentUserRoles().contains(roleName);
    }

    /**
     * Returns true if the current user is a SUPER_ADMIN.
     */
    public boolean isSuperAdmin() {
        return hasRole(SystemRole.SUPER_ADMIN.value());
    }

    /**
     * Returns true if the current user is an ADMIN or higher.
     */
    public boolean isAdminOrAbove() {
        Set<String> roles = getCurrentUserRoles();
        return roles.contains(SystemRole.SUPER_ADMIN.value()) || roles.contains(SystemRole.ADMIN.value());
    }

    /**
     * Returns the highest SystemRole the current user holds, or empty if they have no system role.
     */
    public Optional<SystemRole> getHighestSystemRole() {
        Set<String> roles = getCurrentUserRoles();
        if (roles.contains(SystemRole.SUPER_ADMIN.value())) return Optional.of(SystemRole.SUPER_ADMIN);
        if (roles.contains(SystemRole.ADMIN.value()))       return Optional.of(SystemRole.ADMIN);
        if (roles.contains(SystemRole.CLIENT.value()))      return Optional.of(SystemRole.CLIENT);
        return Optional.empty();
    }

    /**
     * Returns the highest SystemRole held by the given user entity, or CLIENT as a fallback.
     */
    public static SystemRole resolveHighestRole(UserEntity user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) return SystemRole.CLIENT;
        if (user.getRoles().contains(SystemRole.SUPER_ADMIN.value())) return SystemRole.SUPER_ADMIN;
        if (user.getRoles().contains(SystemRole.ADMIN.value()))       return SystemRole.ADMIN;
        return SystemRole.CLIENT;
    }

    /**
     * Returns a convenience string for the highest role of a user.
     */
    public static String resolveHighestRoleName(UserEntity user) {
        return resolveHighestRole(user).value();
    }
}
