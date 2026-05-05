package com.eduai.auth.entity.auth;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Stores a one-time password issued during registration.
 * The OTP is hashed before storage so a DB breach cannot reveal codes.
 * Expired / used OTPs are cleaned up by a scheduled job.
 */
@Entity
@Table(
    name = "otp_records",
    indexes = {
        @Index(name = "otp_email_app_idx", columnList = "email, appId"),
        @Index(name = "otp_expires_idx",   columnList = "expiresAt")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String appId;

    /** BCrypt hash of the 6-digit OTP */
    @Column(nullable = false)
    private String hashedOtp;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    /** Set to true after successful verification — prevents reuse */
    @Column(nullable = false)
    @Builder.Default
    private boolean used = false;
}
