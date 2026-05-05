package com.eduai.auth.repository.user;

import com.eduai.auth.entity.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    /** Used during login — only finds active, non-deleted users for the specified app */
    Optional<UserEntity> findByEmailAndAppIdAndDeletedAtIsNull(String email, String appId);

    /** Used during registration to check uniqueness per app */
    boolean existsByEmailAndAppId(String email, String appId);

    /** Admin: list all users for an app (excluding soft-deleted) */
    Page<UserEntity> findAllByAppIdAndDeletedAtIsNull(String appId, Pageable pageable);

    /** OAuth2: find by provider ID */
    Optional<UserEntity> findByProviderIdAndAppId(String providerId, String appId);

    /**
     * Used by SecurityContextHelper to load the full user entity from the email principal.
     * SecurityContext stores email as the principal name (set in JwtAuthenticationFilter).
     */
    Optional<UserEntity> findByEmail(String email);
}
