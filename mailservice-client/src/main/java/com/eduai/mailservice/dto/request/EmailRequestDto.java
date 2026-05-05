package com.eduai.mailservice.dto.request;

import com.eduai.mailservice.enums.mail.EmailType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Generic email send request DTO.
 * Supports transactional, template-based, and custom HTML emails.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequestDto {

    /** Sender display name (optional — falls back to configured default) */
    private String fromName;

    /** Sender email address (optional — falls back to configured default) */
    @Email(message = "Invalid sender email address")
    private String fromEmail;

    /** Primary recipient email address */
    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid recipient email address")
    private String toEmail;

    /** Recipient display name */
    private String toName;

    /** CC recipients */
    private List<@Email String> cc;

    /** BCC recipients */
    private List<@Email String> bcc;

    /** Email subject line */
    @NotBlank(message = "Subject is required")
    @Size(max = 998, message = "Subject must not exceed 998 characters")
    private String subject;

    /** Type of email — controls template and priority */
    @NotNull(message = "Email type is required")
    private EmailType emailType;

    /**
     * Template variables injected into the Thymeleaf template.
     * Keys map to template variable names.
     */
    private Map<String, Object> templateVariables;

    /**
     * Custom HTML body. Used when emailType = CUSTOM or no template is desired.
     * If provided along with a template type, this overrides template rendering.
     */
    private String htmlBody;

    /** Plain-text fallback body */
    private String textBody;

    /** Reply-to address */
    @Email(message = "Invalid reply-to email")
    private String replyTo;

    /**
     * Preferred email provider key (resend / smtp / sendgrid).
     * If null, the configured default provider is used.
     */
    private String preferredProvider;

    /** Whether to retry on failure (default: true) */
    @Builder.Default
    private boolean retryEnabled = true;

    /** Maximum retry attempts (default: 3) */
    @Builder.Default
    @Min(0) @Max(10)
    private int maxRetries = 3;

    /** Arbitrary metadata (stored in EmailLog for tracing) */
    private Map<String, String> metadata;

    /** Correlation ID for distributed tracing */
    private String correlationId;

    /** Application/tenant identifier for multi-tenant deployments */
    private String appId;

    /** Scheduled send time (null = send immediately) */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledAt;

    /** Whether to track email opens (default: false) */
    @Builder.Default
    private boolean trackOpens = false;

    /** Whether to track link clicks (default: false) */
    @Builder.Default
    private boolean trackClicks = false;
}
