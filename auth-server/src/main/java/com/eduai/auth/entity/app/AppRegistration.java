package com.eduai.auth.entity.app;

import com.eduai.auth.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents an application registered to use the auth-service.
 * Every user belongs to exactly one AppRegistration (via appId).
 * Each app may define its own roles as free strings in UserEntity.
 */
@Entity
@Table(name = "app_registrations", indexes = {
        @Index(name = "app_id_unique_idx", columnList = "appId", unique = true)
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppRegistration extends BaseEntity {

    /** Short identifier — e.g. "eduai", "hrms", "erp". Must be unique. */
    @Column(nullable = false, unique = true, updatable = false)
    private String appId;

    /** Human-readable name — used in emails and error messages */
    @Column(nullable = false)
    private String appName;

    /** Optional display name shown in OTP / welcome emails */
    private String displayName;

    /** Support email shown in OTP emails */
    private String supportEmail;

    /** Frontend success redirect after OAuth2 login */
    private String oauthSuccessRedirectUrl;

    /** Frontend failure redirect after OAuth2 login */
    private String oauthFailureRedirectUrl;

    /** Whether this app is active. Inactive apps reject logins. */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
