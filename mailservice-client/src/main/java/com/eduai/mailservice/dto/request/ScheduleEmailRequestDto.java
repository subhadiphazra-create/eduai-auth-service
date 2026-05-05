package com.eduai.mailservice.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Request DTO for scheduling an email for future delivery.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEmailRequestDto {

    /** The email to schedule */
    @NotNull(message = "Email request payload is required")
    @Valid
    private EmailRequestDto emailRequest;

    /** When to send the email */
    @NotNull(message = "Scheduled time is required")
    @Future(message = "Scheduled time must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledAt;

    /** Optional human-readable label for this scheduled job */
    private String jobLabel;

    /**
     * Cron expression for recurring emails (null = one-shot).
     * Example: "0 9 * * MON" sends every Monday at 09:00.
     */
    private String cronExpression;

    /** Maximum occurrences for recurring emails (0 = unlimited) */
    private Integer maxOccurrences;

    /** Application / tenant ID */
    private String appId;

    /** Correlation ID */
    private String correlationId;
}
