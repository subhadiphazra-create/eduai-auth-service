package com.eduai.auth.security;

import com.eduai.auth.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security UserDetailsService.
 *
 * <p>Note: during login the appId is resolved by the AuthController and passed to
 * {@link #loadUserByEmailAndApp(String, String)}. The standard
 * {@code loadUserByUsername(email)} is NOT used for login — it's kept here only
 * because it is required by the interface; it throws by design so we never
 * accidentally authenticate cross-app.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * DO NOT use for login — use {@link #loadUserByEmailAndApp} instead.
     * Exists only to satisfy the {@link UserDetailsService} contract.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        throw new UnsupportedOperationException(
                "loadUserByUsername is disabled; use loadUserByEmailAndApp()");
    }

    /**
     * Load a user by email + appId.
     * Returns the user only if it is not soft-deleted.
     */
    public UserDetails loadUserByEmailAndApp(String email, String appId) {
        return userRepository.findByEmailAndAppIdAndDeletedAtIsNull(email, appId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email + " for app: " + appId));
    }
}
