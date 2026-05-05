package com.eduai.mailservice.enums.mail;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Lifecycle status of an email message through the processing pipeline.
 */
@Getter
@RequiredArgsConstructor
public enum EmailStatus {

    /** Email has been accepted and queued for processing */
    QUEUED("queued"),

    /** Email is actively being processed */
    PROCESSING("processing"),

    /** Email successfully delivered to provider */
    SENT("sent"),

    /** Email delivery confirmed by provider */
    DELIVERED("delivered"),

    /** Email opened by recipient (if tracking enabled) */
    OPENED("opened"),

    /** Email failed; eligible for retry */
    FAILED("failed"),

    /** Email permanently failed after all retries exhausted */
    PERMANENTLY_FAILED("permanently_failed"),

    /** Email was cancelled before sending */
    CANCELLED("cancelled"),

    /** Email is scheduled for future delivery */
    SCHEDULED("scheduled"),

    /** Scheduled email is pending execution */
    PENDING("pending"),

    /** Email bounced */
    BOUNCED("bounced");

    @JsonValue
    private final String value;

    @JsonCreator
    public static EmailStatus fromValue(String value) {
        for (EmailStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown EmailStatus: " + value);
    }

    public boolean isTerminal() {
        return this == SENT || this == DELIVERED || this == PERMANENTLY_FAILED
                || this == CANCELLED || this == BOUNCED;
    }

    public boolean isRetryable() {
        return this == FAILED;
    }
}
