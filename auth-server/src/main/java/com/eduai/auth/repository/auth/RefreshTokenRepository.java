package com.eduai.auth.repository.auth;

import com.eduai.auth.entity.auth.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByJti(String jti);

    /** Bulk-revoke all tokens belonging to a user — used on logout-all or password change */
    @Modifying
    @Query("UPDATE RefreshTokenEntity r SET r.revoked = true WHERE r.user.id = :userId AND r.revoked = false")
    void revokeAllByUserId(UUID userId);

    /** Clean up expired tokens (scheduled job) */
    @Modifying
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.expiredAt < :cutoff")
    void deleteExpiredBefore(Instant cutoff);
}
