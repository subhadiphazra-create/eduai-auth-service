package com.eduai.auth.controller.auth;

import com.eduai.auth.api.auth.AuthAPI;
import com.eduai.auth.dto.request.token.RefreshTokenRequest;
import com.eduai.auth.dto.request.token.TokenResponse;
import com.eduai.auth.dto.request.user.LoginDTO;
import com.eduai.auth.dto.request.user.RegisterDTO;
import com.eduai.auth.dto.request.user.VerifyOtpDTO;
import com.eduai.auth.dto.response.user.UserResponseDTO;
import com.eduai.auth.entity.auth.RefreshTokenEntity;
import com.eduai.auth.entity.user.UserEntity;
import com.eduai.auth.exception.AppNotFoundException;
import com.eduai.auth.mapper.user.UserMapper;
import com.eduai.auth.repository.app.AppRegistrationRepository;
import com.eduai.auth.repository.auth.RefreshTokenRepository;
import com.eduai.auth.repository.user.UserRepository;
import com.eduai.auth.security.JwtService;
import com.eduai.auth.service.impl.auth.CookieService;
import com.eduai.auth.service.user.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController implements AuthAPI {

    private final UserRepository            userRepository;
    private final AppRegistrationRepository appRegistrationRepository;
    private final RefreshTokenRepository    refreshTokenRepository;
    private final JwtService                jwtService;
    private final UserMapper                userMapper;
    private final CookieService             cookieService;
    private final UserService               userService;
    private final PasswordEncoder           passwordEncoder;

    // ── Register ──────────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<UserResponseDTO> register(@Valid RegisterDTO request) {
        UserResponseDTO response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── Verify OTP ────────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<TokenResponse> verifyOtp(@Valid VerifyOtpDTO request,
                                                    HttpServletResponse response) {
        TokenResponse tokenResponse = userService.verifyOtp(request, response);
        return ResponseEntity.ok(tokenResponse);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<TokenResponse> login(@Valid LoginDTO loginRequest,
                                               HttpServletResponse response) {

        // Validate app exists and is active
        appRegistrationRepository.findByAppIdAndActiveTrue(loginRequest.getAppId())
                .orElseThrow(() -> new AppNotFoundException(
                        "No active application found with appId: " + loginRequest.getAppId()));

        // Load user by email + appId (scope login to the correct app namespace)
        UserEntity user = userRepository
                .findByEmailAndAppIdAndDeletedAtIsNull(loginRequest.getEmail(), loginRequest.getAppId())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        if (!user.isEnabled()) {
            if (!Boolean.TRUE.equals(user.getEmailVerified())) {
                throw new DisabledException("Account not verified. Please check your email for the OTP.");
            }
            throw new DisabledException("Account is disabled. Please contact support.");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        String jti = UUID.randomUUID().toString();
        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                .jti(jti)
                .user(user)
                .revoked(false)
                .createdAt(Instant.now())
                .expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        String accessToken  = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, jti);

        cookieService.attachRefreshCookie(response, refreshToken, (int) jwtService.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);

        UserResponseDTO userDto = userMapper.toResponse(user);
        return ResponseEntity.ok(
                TokenResponse.of(accessToken, refreshToken, jwtService.getAccessTtlSeconds(), userDto));
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<TokenResponse> refreshToken(RefreshTokenRequest body,
                                                       HttpServletResponse response,
                                                       HttpServletRequest request) {

        String refreshToken = readRefreshToken(body, request)
                .orElseThrow(() -> new BadCredentialsException("Refresh token not found."));

        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token type.");
        }

        String jti    = jwtService.getJti(refreshToken);
        UUID   userId = jwtService.getUserId(refreshToken);

        RefreshTokenEntity stored = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new BadCredentialsException("Refresh token not recognised."));

        if (stored.isRevoked()) {
            // Possible token-reuse attack — revoke entire family
            log.warn("Refresh token reuse detected for userId={}", userId);
            refreshTokenRepository.revokeAllByUserId(userId);
            throw new BadCredentialsException("Refresh token has already been used or revoked.");
        }

        if (stored.getExpiredAt().isBefore(Instant.now())) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new BadCredentialsException("Refresh token has expired. Please log in again.");
        }

        if (!stored.getUser().getId().equals(userId)) {
            throw new BadCredentialsException("Refresh token does not belong to this user.");
        }

        // Rotate: revoke old, issue new
        String newJti = UUID.randomUUID().toString();
        stored.setRevoked(true);
        stored.setReplacedByToken(newJti);
        refreshTokenRepository.save(stored);

        UserEntity user = stored.getUser();

        RefreshTokenEntity newTokenEntity = RefreshTokenEntity.builder()
                .jti(newJti)
                .user(user)
                .revoked(false)
                .createdAt(Instant.now())
                .expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .build();
        refreshTokenRepository.save(newTokenEntity);

        String newAccessToken  = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user, newJti);

        cookieService.attachRefreshCookie(response, newRefreshToken, (int) jwtService.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);

        return ResponseEntity.ok(
                TokenResponse.of(newAccessToken, newRefreshToken, jwtService.getAccessTtlSeconds(),
                        userMapper.toResponse(user)));
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        readRefreshToken(null, request).ifPresent(token -> {
            try {
                if (jwtService.isRefreshToken(token)) {
                    String jti = jwtService.getJti(token);
                    refreshTokenRepository.findByJti(jti).ifPresent(rt -> {
                        rt.setRevoked(true);
                        refreshTokenRepository.save(rt);
                    });
                }
            } catch (JwtException ignored) {
                // Token may already be expired — still clear the cookie
            }
        });

        cookieService.clearRefreshCookie(response);
        cookieService.addNoStoreHeaders(response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Optional<String> readRefreshToken(RefreshTokenRequest body, HttpServletRequest request) {
        // 1. HTTP-only cookie (most secure)
        if (request != null && request.getCookies() != null) {
            Optional<String> fromCookie = Arrays.stream(request.getCookies())
                    .filter(c -> cookieService.getRefreshTokenCookieName().equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v -> v != null && !v.isBlank())
                    .findFirst();
            if (fromCookie.isPresent()) return fromCookie;
        }

        // 2. Request body
        if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
            return Optional.of(body.refreshToken());
        }

        // 3. Custom header
        if (request != null) {
            String header = request.getHeader("X-Refresh-Token");
            if (header != null && !header.isBlank()) return Optional.of(header.trim());

            // 4. Authorization header (Bearer)
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
                String candidate = authHeader.substring(7).trim();
                if (!candidate.isEmpty()) {
                    try {
                        if (jwtService.isRefreshToken(candidate)) return Optional.of(candidate);
                    } catch (Exception ignored) {}
                }
            }
        }

        return Optional.empty();
    }
}
