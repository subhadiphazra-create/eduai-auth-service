package com.eduai.auth.service.impl.user;

import com.eduai.auth.dto.request.token.TokenResponse;
import com.eduai.auth.dto.request.user.ChangePasswordDTO;
import com.eduai.auth.dto.request.user.RegisterDTO;
import com.eduai.auth.dto.request.user.VerifyOtpDTO;
import com.eduai.auth.dto.response.user.UserResponseDTO;
import com.eduai.auth.entity.app.AppRegistration;
import com.eduai.auth.entity.auth.RefreshTokenEntity;
import com.eduai.auth.entity.user.UserEntity;
import com.eduai.auth.enums.role.SystemRole;
import com.eduai.auth.enums.user.ProviderENUM;
import com.eduai.auth.exception.AppNotFoundException;
import com.eduai.auth.exception.ResourceAlreadyExistsException;
import com.eduai.auth.exception.ResourceNotFoundException;
import com.eduai.auth.mapper.user.UserMapper;
import com.eduai.auth.repository.app.AppRegistrationRepository;
import com.eduai.auth.repository.auth.RefreshTokenRepository;
import com.eduai.auth.repository.user.UserRepository;
import com.eduai.auth.security.JwtService;
import com.eduai.auth.service.impl.auth.CookieService;
import com.eduai.auth.service.impl.auth.OtpService;
import com.eduai.auth.service.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository            userRepository;
    private final AppRegistrationRepository appRegistrationRepository;
    private final RefreshTokenRepository    refreshTokenRepository;
    private final PasswordEncoder           passwordEncoder;
    private final UserMapper                userMapper;
    private final JwtService                jwtService;
    private final CookieService             cookieService;
    private final OtpService                otpService;

    @Override
    @Transactional
    public UserResponseDTO register(RegisterDTO dto) {
        AppRegistration app = resolveApp(dto.getAppId());
        if (userRepository.existsByEmailAndAppId(dto.getEmail(), dto.getAppId())) {
            throw new ResourceAlreadyExistsException(
                "An account with email '" + dto.getEmail() + "' already exists in " + app.getAppName());
        }

        // Every new user gets the default CLIENT role on registration
        UserEntity user = UserEntity.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .appId(dto.getAppId())
                .provider(ProviderENUM.LOCAL)
                .enabled(false)
                .emailVerified(false)
                .roles(Set.of(SystemRole.CLIENT.value())) // Default role: CLIENT
                .build();

        user = userRepository.save(user);
        otpService.issueOtp(user, app);

        log.info("Registered user '{}' in app '{}' with default role CLIENT", dto.getEmail(), dto.getAppId());

        UserResponseDTO response = userMapper.toResponse(user);
        response.setMessage("Registration successful. Please check your email for the OTP.");
        return response;
    }

    @Override
    @Transactional
    public TokenResponse verifyOtp(VerifyOtpDTO dto, HttpServletResponse response) {
        resolveApp(dto.getAppId());
        UserEntity user = userRepository
                .findByEmailAndAppIdAndDeletedAtIsNull(dto.getEmail(), dto.getAppId())
                .orElseThrow(() -> new ResourceNotFoundException("No account found for: " + dto.getEmail()));

        if (user.isEnabled() && Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new ResourceAlreadyExistsException("Account already verified. Please log in.");
        }

        otpService.verifyOtp(dto.getEmail(), dto.getAppId(), dto.getOtp());
        user.setEnabled(true);
        user.setEmailVerified(true);
        user = userRepository.save(user);

        String jti = UUID.randomUUID().toString();
        refreshTokenRepository.save(RefreshTokenEntity.builder()
                .jti(jti).user(user).revoked(false)
                .createdAt(Instant.now())
                .expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .build());

        String accessToken  = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, jti);
        cookieService.attachRefreshCookie(response, refreshToken, (int) jwtService.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);

        UserResponseDTO userDto = userMapper.toResponse(user);
        userDto.setMessage("Email verified. You are now logged in.");
        return TokenResponse.of(accessToken, refreshToken, jwtService.getAccessTtlSeconds(), userDto);
    }

    @Override
    @Transactional
    public UserResponseDTO changePassword(ChangePasswordDTO dto) {
        // Extract email from security context (authentication principal)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // Resolve appId from the authenticated user record
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + email));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect.");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        refreshTokenRepository.revokeAllByUserId(user.getId());
        user = userRepository.save(user);

        UserResponseDTO res = userMapper.toResponse(user);
        res.setMessage("Password changed. Please log in again.");
        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(String email, String appId) {
        return userMapper.toResponse(findActiveUser(email, appId));
    }

    @Override
    @Transactional
    public UserResponseDTO softDeleteUser(String email, String appId) {
        UserEntity user = findActiveUser(email, appId);
        user.setDeletedAt(Instant.now());
        user.setEnabled(false);
        user = userRepository.save(user);
        refreshTokenRepository.revokeAllByUserId(user.getId());

        UserResponseDTO res = userMapper.toResponse(user);
        res.setMessage("User deleted successfully.");
        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getAllUsers(String appId, Pageable pageable) {
        return userRepository.findAllByAppIdAndDeletedAtIsNull(appId, pageable).map(userMapper::toResponse);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AppRegistration resolveApp(String appId) {
        return appRegistrationRepository.findByAppIdAndActiveTrue(appId)
                .orElseThrow(() -> new AppNotFoundException("No active app found: " + appId));
    }

    private UserEntity findActiveUser(String email, String appId) {
        return userRepository.findByEmailAndAppIdAndDeletedAtIsNull(email, appId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
