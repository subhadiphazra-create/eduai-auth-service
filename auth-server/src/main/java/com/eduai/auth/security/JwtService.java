package com.eduai.auth.security;

import com.eduai.auth.entity.user.UserEntity;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * JWT utility service.
 *
 * <h3>Bugs fixed from original:</h3>
 * <ol>
 *   <li>{@code isRefreshToken()} was checking {@code "typ"} but {@code generateAccessToken()}
 *       wrote {@code "type"} — now consistent: the claim key is {@code "type"}.</li>
 *   <li>{@code generateRefreshToken()} ignored the {@code jti} parameter and generated
 *       a random UUID instead — now uses the supplied JTI so token rotation works correctly.</li>
 *   <li>Roles are stored as {@code List<String>} (not enum) — compatible with dynamic roles.</li>
 *   <li>AppId is embedded in both access and refresh tokens for multi-app validation.</li>
 * </ol>
 */
@Service
@Getter
public class JwtService {

    private final SecretKey key;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;
    private final String issuer;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-ttl-seconds}") long accessTtlSeconds,
            @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds,
            @Value("${security.jwt.issuer}") String issuer
    ) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 64) {
            throw new IllegalArgumentException(
                    "JWT secret must be at least 64 bytes (512 bits) for HS512");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.issuer = issuer;
    }

    // ── Token generation ──────────────────────────────────────────────────────

    public String generateAccessToken(UserEntity user) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles() == null ? List.of() : List.copyOf(user.getRoles());

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .claims(Map.of(
                        "email", user.getEmail(),
                        "roles", roles,
                        "appId", user.getAppId(),
                        "type",  "access"           // FIX: was "typ" in isAccessToken() check
                ))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * @param jti the pre-generated JTI that is also persisted in {@code RefreshTokenEntity}.
     *            Must be supplied by the caller — NOT auto-generated here —
     *            so the stored record and the token's JTI always match.
     */
    public String generateRefreshToken(UserEntity user, String jti) {
        Instant now = Instant.now();

        return Jwts.builder()
                .id(jti)                            // FIX: original ignored jti param
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
                .claims(Map.of(
                        "appId", user.getAppId(),
                        "type",  "refresh"
                ))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // ── Parsing ───────────────────────────────────────────────────────────────

    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }

    public boolean isAccessToken(String token) {
        Claims c = parse(token).getPayload();
        return "access".equals(c.get("type"));      // FIX: was "typ"
    }

    public boolean isRefreshToken(String token) {
        Claims c = parse(token).getPayload();
        return "refresh".equals(c.get("type"));     // FIX: was "typ"
    }

    public UUID getUserId(String token) {
        return UUID.fromString(parse(token).getPayload().getSubject());
    }

    public String getJti(String token) {
        return parse(token).getPayload().getId();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        return (List<String>) parse(token).getPayload().get("roles");
    }

    public String getEmail(String token) {
        return (String) parse(token).getPayload().get("email");
    }

    public String getAppId(String token) {
        return (String) parse(token).getPayload().get("appId");
    }
}
