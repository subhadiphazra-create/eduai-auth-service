package com.eduai.mailservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Map;

/**
 * Request DTO for sending OTP verification emails.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpRequestDto {

    /** Recipient email address */
    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid recipient email address")
    private String toEmail;

    /** Recipient display name */
    private String toName;

    /**
     * Pre-generated OTP value.
     * If null, the service will generate one automatically.
     */
    @Size(min = 4, max = 10, message = "OTP length must be between 4 and 10 digits")
    private String otp;

    /**
     * OTP length to generate (used only when otp is null).
     * Default: 6
     */
    @Builder.Default
    @Min(4) @Max(10)
    private int otpLength = 6;

    /**
     * OTP expiry in minutes.
     * Default: 10
     */
    @Builder.Default
    @Min(1) @Max(60)
    private int expiryMinutes = 10;

    /** Purpose label displayed in the email (e.g., "Login Verification", "Account Activation") */
    @Builder.Default
    private String purpose = "Verification";

    /** Application name shown in the email */
    private String appName;

    /** Application logo URL (optional) */
    private String appLogoUrl;

    /** Support email shown in the email footer */
    private String supportEmail;

    /** Correlation ID for tracing */
    private String correlationId;

    /** Application / tenant ID */
    private String appId;

    /** Preferred provider override */
    private String preferredProvider;

    /** Extra template variables */
    private Map<String, Object> additionalVariables;
}
