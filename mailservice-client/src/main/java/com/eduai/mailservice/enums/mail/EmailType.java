package com.eduai.mailservice.enums.mail;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents the type/category of an email being sent.
 * Controls template selection, priority, and processing pipeline.
 */
@Getter
@RequiredArgsConstructor
public enum EmailType {

    /** One-Time Password verification email */
    OTP("otp", "otp", 1),

    /** Welcome email for new users */
    WELCOME("welcome", "welcome", 2),

    /** General transactional email */
    TRANSACTIONAL("transactional", "transactional", 2),

    /** Reminder email (e.g., course deadlines) */
    REMINDER("reminder", "reminder", 3),

    /** Promotional/marketing email */
    PROMOTIONAL("promotional", "promo", 5),

    /** General system notification */
    NOTIFICATION("notification", "notification", 3),

    /** Account update / security alert */
    UPDATE("update", "update", 2),

    /** Password reset email */
    PASSWORD_RESET("password_reset", "password_reset", 1),

    /** Bulk broadcast email */
    BULK("bulk", "promo", 5),

    /** Custom email with caller-supplied HTML */
    CUSTOM("custom", null, 4);

    /** Machine-readable value stored in DB */
    @JsonValue
    private final String value;

    /** Thymeleaf template name (null = custom HTML supplied) */
    private final String templateName;

    /**
     * Processing priority: lower = higher priority.
     * OTP and password-reset are always priority 1.
     */
    private final int priority;

    @JsonCreator
    public static EmailType fromValue(String value) {
        for (EmailType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown EmailType: " + value);
    }

    public boolean isHighPriority() {
        return priority <= 2;
    }

    public boolean requiresTemplate() {
        return templateName != null;
    }
}
