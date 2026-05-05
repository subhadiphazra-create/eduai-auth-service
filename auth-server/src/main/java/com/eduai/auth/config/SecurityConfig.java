package com.eduai.auth.config;

import com.eduai.auth.dto.response.user.ApiError;
import com.eduai.auth.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the auth-service.
 *
 * <p>{@code @EnableMethodSecurity} enables {@code @PreAuthorize} on controller methods
 * for fine-grained RBAC without any role enum constraints.
 *
 * <p>Changes from original:
 * <ul>
 *   <li>Swagger/OpenAPI endpoints are now permitted for development convenience.</li>
 *   <li>Role management and admin app endpoints properly require authentication.</li>
 *   <li>AccessDeniedException is now handled via GlobalExceptionHandler (Spring Security
 *       AccessDeniedException is re-thrown into the handler chain).</li>
 * </ul>
 */
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper            objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public auth endpoints
                        .requestMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/verify-otp",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout"
                        ).permitAll()
                        // OAuth2 endpoints
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        // Actuator health only
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // Swagger / OpenAPI (disable in production via application properties)
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs"
                        ).permitAll()
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json;charset=UTF-8");

                            String errorMsg = "Unauthorized access.";
                            String attrError = (String) request.getAttribute("error");
                            if (attrError != null && !attrError.isBlank()) {
                                errorMsg = attrError;
                            }

                            ApiError apiError = ApiError.of(
                                    HttpStatus.UNAUTHORIZED.value(),
                                    "Unauthorized",
                                    errorMsg,
                                    request.getRequestURI(),
                                    true
                            );
                            response.getWriter().write(objectMapper.writeValueAsString(apiError));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json;charset=UTF-8");
                            ApiError apiError = ApiError.of(
                                    HttpStatus.FORBIDDEN.value(),
                                    "Forbidden",
                                    accessDeniedException.getMessage() != null
                                        ? accessDeniedException.getMessage()
                                        : "You do not have permission to access this resource.",
                                    request.getRequestURI()
                            );
                            response.getWriter().write(objectMapper.writeValueAsString(apiError));
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
