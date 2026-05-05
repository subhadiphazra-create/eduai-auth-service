package com.eduai.mailservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for OTP send operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OtpResponseDto {

    /** Email message ID */
    private String messageId;

    /** Recipient email (masked for security) */
    private String maskedEmail;

    /** OTP sent successfully */
    private boolean sent;

    /** OTP expiry time */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;

    /** Expiry in minutes */
    private int expiryMinutes;

    /** Status message */
    private String message;

    /** Correlation ID */
    private String correlationId;

    public static OtpResponseDto success(String messageId, String email, int expiryMinutes, String correlationId) {
        return OtpResponseDto.builder()
                .messageId(messageId)
                .maskedEmail(maskEmail(email))
                .sent(true)
                .expiryMinutes(expiryMinutes)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .message("OTP sent successfully")
                .correlationId(correlationId)
                .build();
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        String local = parts[0];
        String masked = local.length() <= 2
                ? "**"
                : local.charAt(0) + "*".repeat(local.length() - 2) + local.charAt(local.length() - 1);
        return masked + "@" + parts[1];
    }
}
