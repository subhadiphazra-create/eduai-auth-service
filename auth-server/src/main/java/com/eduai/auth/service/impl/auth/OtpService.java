package com.eduai.auth.service.impl.auth;

import com.eduai.auth.entity.app.AppRegistration;
import com.eduai.auth.entity.auth.OtpEntity;
import com.eduai.auth.entity.user.UserEntity;
import com.eduai.auth.exception.ResourceNotFoundException;
import com.eduai.auth.feign.MailServiceFeignClient;
import com.eduai.auth.repository.auth.OtpRepository;
import com.eduai.mailservice.api.mail.MailServiceClient;
import com.eduai.mailservice.dto.request.OtpRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * Generates, persists (hashed), sends, and verifies OTPs.
 *
 * <p>OTPs are hashed with BCrypt before storage so a database dump
 * cannot be used to verify emails without the original codes.
 *
 * <p>The plain-text OTP is only sent to the mail-service (over internal
 * network) and immediately discarded — it is never logged or persisted.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final OtpRepository   otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailServiceFeignClient mailServiceFeignClient;

    @Value("${auth.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    @Value("${auth.otp.length:6}")
    private int otpLength;

    // ── Generate + Send ───────────────────────────────────────────────────────

    /**
     * Generates a new OTP, invalidates any previous ones for this email+app,
     * persists the hashed value, and sends the plain OTP via the mail-service.
     */
    @Transactional
    public void issueOtp(UserEntity user, AppRegistration app) {
        String plainOtp = generateNumericOtp(otpLength);

        // Invalidate previous un-used OTPs so only the latest is valid
        otpRepository.invalidateAllFor(user.getEmail(), app.getAppId());

        OtpEntity otp = OtpEntity.builder()
                .email(user.getEmail())
                .appId(app.getAppId())
                .hashedOtp(passwordEncoder.encode(plainOtp))
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(otpExpiryMinutes * 60L))
                .used(false)
                .build();
        otpRepository.save(otp);

        // Send plain OTP to mail-service — fire-and-forget with error logging
        try {
            OtpRequestDto mailRequest = OtpRequestDto.builder()
                    .toEmail(user.getEmail())
                    .toName(user.getName())
                    .otp(plainOtp)
                    .purpose("Email Verification")
                    .appName(app.getAppName())
                    .appId(app.getAppId())
                    .supportEmail(app.getSupportEmail())
                    .expiryMinutes(otpExpiryMinutes)
                    .otpLength(otpLength)
                    .build();
            mailServiceFeignClient.sendOtp(mailRequest);
            log.info("OTP sent to {} for app {}", user.getEmail(), app.getAppId());
        } catch (Exception e) {
            // Log but don't fail — user can request resend
            log.error("Failed to send OTP email to {} (app={}): {}",
                    user.getEmail(), app.getAppId(), e.getMessage());
        }
    }

    // ── Verify ────────────────────────────────────────────────────────────────

    /**
     * Validates the supplied OTP against the stored hash.
     * Marks the OTP as used on success to prevent replay.
     *
     * @throws BadCredentialsException if OTP is invalid or expired
     */
    @Transactional
    public void verifyOtp(String email, String appId, String plainOtp) {
        OtpEntity stored = otpRepository
                .findLatestValid(email, appId, Instant.now())
                .orElseThrow(() -> new BadCredentialsException(
                        "OTP is invalid or has expired. Please request a new one."));

        if (!passwordEncoder.matches(plainOtp, stored.getHashedOtp())) {
            throw new BadCredentialsException("Incorrect OTP. Please try again.");
        }

        stored.setUsed(true);
        otpRepository.save(stored);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String generateNumericOtp(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
