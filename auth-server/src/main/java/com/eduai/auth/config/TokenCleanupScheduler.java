package com.eduai.auth.config;

import com.eduai.auth.repository.auth.OtpRepository;
import com.eduai.auth.repository.auth.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Scheduled housekeeping tasks.
 * Keeps the DB clean by removing expired refresh tokens and OTPs.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpRepository          otpRepository;

    /** Runs every hour. Deletes refresh tokens that have expired. */
    @Scheduled(fixedRateString = "${auth.cleanup.refresh-token-interval-ms:3600000}")
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        try {
            refreshTokenRepository.deleteExpiredBefore(Instant.now());
            log.debug("Expired refresh tokens cleaned up.");
        } catch (Exception e) {
            log.error("Error during refresh-token cleanup: {}", e.getMessage());
        }
    }

    /** Runs every 15 minutes. Deletes expired OTP records. */
    @Scheduled(fixedRateString = "${auth.cleanup.otp-interval-ms:900000}")
    @Transactional
    public void cleanupExpiredOtps() {
        try {
            otpRepository.deleteExpiredBefore(Instant.now());
            log.debug("Expired OTPs cleaned up.");
        } catch (Exception e) {
            log.error("Error during OTP cleanup: {}", e.getMessage());
        }
    }
}
