package com.eduai.auth.security;

import com.eduai.auth.repository.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Validates the JWT access token on every request.
 *
 * <h3>Bugs fixed from original:</h3>
 * <ol>
 *   <li>{@code userRepository} was declared but never injected (missing {@code @RequiredArgsConstructor}
 *       / no constructor injection) — it was always {@code null}, causing NPE on every request.</li>
 *   <li>Authentication was only set when {@code SecurityContextHolder.getContext().getAuthentication() != null}
 *       — i.e., it was only set if someone was already authenticated, which is the opposite of correct.</li>
 *   <li>Roles are now read from the token directly — no DB call per request, more scalable.</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();

        try {
            Jws<Claims> jws = jwtService.parse(token);
            Claims payload = jws.getPayload();

            // Reject refresh tokens presented as access tokens
            if (!"access".equals(payload.get("type"))) {
                request.setAttribute("error", "Invalid token type.");
                filterChain.doFilter(request, response);
                return;
            }

            UUID userId = UUID.fromString(payload.getSubject());

            // Verify the user still exists and is enabled
            userRepository.findById(userId).ifPresentOrElse(user -> {
                if (!user.isEnabled()) {
                    request.setAttribute("error", "Account is disabled.");
                    return;
                }
                if (!user.isAccountNonExpired()) {
                    request.setAttribute("error", "Account has been deleted.");
                    return;
                }

                // Build authorities from token claims (avoids DB call per request)
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) payload.getOrDefault("roles", List.of());
                List<GrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // FIX: was only setting auth when context already had one (inverted condition)
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            }, () -> request.setAttribute("error", "User not found."));

        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
            request.setAttribute("error", "Token has expired.");
        } catch (JwtException e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            request.setAttribute("error", "Invalid token.");
        } catch (Exception e) {
            log.warn("Unexpected error during JWT validation: {}", e.getMessage());
            request.setAttribute("error", "Authentication failed.");
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/api/v1/auth/login")
                || uri.startsWith("/api/v1/auth/register")
                || uri.startsWith("/api/v1/auth/verify-otp")
                || uri.startsWith("/oauth2/")
                || uri.startsWith("/login/oauth2/");
    }
}
