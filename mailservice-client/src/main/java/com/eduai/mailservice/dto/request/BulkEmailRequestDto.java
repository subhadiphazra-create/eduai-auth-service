package com.eduai.mailservice.dto.request;

import com.eduai.mailservice.enums.mail.EmailType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for bulk/broadcast email sending.
 * Supports per-recipient variable substitution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkEmailRequestDto {

    /** Sender display name */
    private String fromName;

    /** Sender email address */
    @Email(message = "Invalid sender email address")
    private String fromEmail;

    /** Email subject (supports variable placeholders like {name}) */
    @NotBlank(message = "Subject is required")
    @Size(max = 998)
    private String subject;

    /** Email type for all messages in this batch */
    @NotNull(message = "Email type is required")
    private EmailType emailType;

    /** List of individual recipient entries */
    @NotEmpty(message = "At least one recipient is required")
    @Size(max = 1000, message = "Maximum 1000 recipients per bulk request")
    @Valid
    private List<BulkRecipient> recipients;

    /** Global template variables applied to all recipients (can be overridden per-recipient) */
    private Map<String, Object> globalTemplateVariables;

    /** Custom HTML body (used when emailType = CUSTOM) */
    private String htmlBody;

    /** Application/tenant ID */
    private String appId;

    /** Correlation ID for tracing */
    private String correlationId;

    /** Preferred provider */
    private String preferredProvider;

    /** Whether to retry failed sends */
    @Builder.Default
    private boolean retryEnabled = true;

    /** Rate limit: max emails per second (0 = unlimited) */
    @Builder.Default
    @Min(0) @Max(100)
    private int rateLimitPerSecond = 10;

    /** Scheduled send time (null = send immediately) */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledAt;

    // ── Inner: per-recipient entry ──────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkRecipient {

        @NotBlank(message = "Recipient email is required")
        @Email(message = "Invalid recipient email")
        private String email;

        private String name;

        /** Per-recipient template variable overrides */
        private Map<String, Object> templateVariables;

        /** Per-recipient metadata */
        private Map<String, String> metadata;
    }
}
