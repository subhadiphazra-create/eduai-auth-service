package com.eduai.auth.entity.auth;

import com.eduai.auth.entity.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(name = "refresh_token_jti_idx",    columnList = "jti",     unique = true),
                @Index(name = "refresh_token_user_id_idx", columnList = "user_id")
        }
)
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @Column(nullable = false, unique = true, updatable = false)
    private String jti;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private UserEntity user;

    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiredAt;

    @Column(nullable = false)
    private boolean revoked;

    /** JTI of the token that replaced this one (token rotation chain) */
    private String replacedByToken;
}
