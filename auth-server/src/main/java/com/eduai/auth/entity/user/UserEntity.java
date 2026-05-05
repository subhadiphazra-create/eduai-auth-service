package com.eduai.auth.entity.user;

import com.eduai.auth.entity.app.AppRegistration;
import com.eduai.auth.entity.base.BaseEntity;
import com.eduai.auth.enums.user.ProviderENUM;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Central user entity shared across all applications.
 *
 * <p>Key design decisions:
 * <ul>
 *   <li>Roles are stored as plain strings — no enum — so every registered app
 *       can use its own role names (e.g. "TEACHER", "STUDENT", "HR_MANAGER").</li>
 *   <li>Each user belongs to exactly one {@link AppRegistration} via {@code appId}.
 *       This separates user namespaces across apps even when the same email exists
 *       in multiple apps.</li>
 *   <li>Email verification is mandatory before login is allowed.</li>
 *   <li>Soft-delete via {@code deletedAt} — no hard removal of records.</li>
 * </ul>
 */
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(
        name = "auth_users",
        indexes = {
                @Index(name = "user_email_app_idx", columnList = "email, appId", unique = true)
        }
)
public class UserEntity extends BaseEntity implements UserDetails {

    @Column(nullable = false)
    private String name;

    /** Email is unique per app (email + appId combination must be unique) */
    @Column(nullable = false, updatable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    /**
     * App this user belongs to — mirrors AppRegistration.appId.
     * Stored as a plain string for fast lookup without a join.
     */
    @Column(nullable = false, updatable = false)
    private String appId;

    /**
     * Dynamic roles — stored as a collection of strings.
     * No enum so each app defines its own roles (ADMIN, TEACHER, HR_MANAGER, etc.)
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            indexes = @Index(name = "user_roles_user_idx", columnList = "user_id")
    )
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    /** Whether the account is active (admin-controlled) */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = false;        // false until OTP verified

    /** Whether the email has been OTP-verified */
    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Builder.Default
    private ProviderENUM provider = ProviderENUM.LOCAL;

    private String image;

    /** Populated for OAuth2 users */
    private String providerId;

    private Instant deletedAt;

    // ─── UserDetails ──────────────────────────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null) return Set.of();
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return deletedAt == null || deletedAt.isAfter(Instant.now());
    }

    @Override
    public boolean isAccountNonLocked() {
        return deletedAt == null || deletedAt.isAfter(Instant.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(this.enabled);
    }
}
