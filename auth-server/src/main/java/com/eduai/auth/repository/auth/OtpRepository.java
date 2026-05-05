package com.eduai.auth.repository.auth;

import com.eduai.auth.entity.auth.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntity, UUID> {

    /**
     * Latest unused, unexpired OTP for this email + app combination.
     * Ordered by createdAt DESC so the most recently issued OTP wins
     * when a user requests a resend.
     */
    @Query("""
        SELECT o FROM OtpEntity o
        WHERE o.email = :email AND o.appId = :appId
          AND o.used = false AND o.expiresAt > :now
        ORDER BY o.createdAt DESC
        LIMIT 1
        """)
    Optional<OtpEntity> findLatestValid(String email, String appId, Instant now);

    /** Scheduled cleanup — remove all expired OTPs to keep the table small */
    @Modifying
    @Query("DELETE FROM OtpEntity o WHERE o.expiresAt < :cutoff")
    void deleteExpiredBefore(Instant cutoff);

    /** Invalidate all previous OTPs for this user (e.g. on resend) */
    @Modifying
    @Query("UPDATE OtpEntity o SET o.used = true WHERE o.email = :email AND o.appId = :appId AND o.used = false")
    void invalidateAllFor(String email, String appId);
}
