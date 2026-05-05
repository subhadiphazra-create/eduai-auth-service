package com.eduai.mailservice.dto.response;

import com.eduai.mailservice.enums.mail.EmailStatus;
import com.eduai.mailservice.enums.mail.EmailType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard response DTO for all mail service operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailResponseDto {

    /** Unique message ID (UUID) */
    private String messageId;

    /** Current status of the email */
    private EmailStatus status;

    /** Type of email that was processed */
    private EmailType emailType;

    /** Recipient email address */
    private String toEmail;

    /** Provider-assigned message ID (for delivery tracking) */
    private String providerMessageId;

    /** Which provider processed this email */
    private String providerUsed;

    /** Human-readable status message */
    private String message;

    /** Whether the request was accepted successfully */
    private boolean accepted;

    /** Whether the email is queued for async processing */
    private boolean queued;

    /** Scheduled delivery time (if applicable) */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledAt;

    /** When the email was accepted */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime acceptedAt;

    /** When the email was sent */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sentAt;

    /** Correlation ID echoed back */
    private String correlationId;

    /** Validation or processing errors */
    private Map<String, String> errors;

    /** Estimated delivery info */
    private String estimatedDelivery;

    // ── Convenience factory methods ─────────────────────────────────────────

    public static EmailResponseDto accepted(String messageId, String toEmail, EmailType type, String correlationId) {
        return EmailResponseDto.builder()
                .messageId(messageId)
                .toEmail(toEmail)
                .emailType(type)
                .status(EmailStatus.QUEUED)
                .accepted(true)
                .queued(true)
                .message("Email accepted and queued for delivery")
                .acceptedAt(LocalDateTime.now())
                .correlationId(correlationId)
                .build();
    }

    public static EmailResponseDto sent(String messageId, String toEmail, EmailType type,
                                        String providerMessageId, String provider) {
        return EmailResponseDto.builder()
                .messageId(messageId)
                .toEmail(toEmail)
                .emailType(type)
                .status(EmailStatus.SENT)
                .accepted(true)
                .queued(false)
                .providerMessageId(providerMessageId)
                .providerUsed(provider)
                .message("Email sent successfully")
                .acceptedAt(LocalDateTime.now())
                .sentAt(LocalDateTime.now())
                .build();
    }

    public static EmailResponseDto scheduled(String messageId, String toEmail, EmailType type,
                                             LocalDateTime scheduledAt) {
        return EmailResponseDto.builder()
                .messageId(messageId)
                .toEmail(toEmail)
                .emailType(type)
                .status(EmailStatus.SCHEDULED)
                .accepted(true)
                .queued(false)
                .scheduledAt(scheduledAt)
                .message("Email scheduled for delivery at " + scheduledAt)
                .acceptedAt(LocalDateTime.now())
                .build();
    }

    public static EmailResponseDto failed(String messageId, String toEmail, String reason) {
        return EmailResponseDto.builder()
                .messageId(messageId)
                .toEmail(toEmail)
                .status(EmailStatus.FAILED)
                .accepted(false)
                .queued(false)
                .message("Email delivery failed: " + reason)
                .acceptedAt(LocalDateTime.now())
                .build();
    }
}
