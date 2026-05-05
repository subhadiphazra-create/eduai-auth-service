package com.eduai.auth.api.auth;

import com.eduai.auth.dto.request.token.RefreshTokenRequest;
import com.eduai.auth.dto.request.token.TokenResponse;
import com.eduai.auth.dto.request.user.LoginDTO;
import com.eduai.auth.dto.request.user.RegisterDTO;
import com.eduai.auth.dto.request.user.VerifyOtpDTO;
import com.eduai.auth.dto.response.user.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/auth")
public interface AuthAPI {

    @Operation(summary = "Register a new user — sends OTP for email verification")
    @ApiResponse(responseCode = "201", description = "Registered; OTP sent to email")
    @ApiResponse(responseCode = "400", description = "Validation error or email already exists")
    @PostMapping("/register")
    ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterDTO request);

    @Operation(summary = "Verify email via OTP — activates the account")
    @ApiResponse(responseCode = "200", description = "Email verified; returns tokens")
    @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    @PostMapping("/verify-otp")
    ResponseEntity<TokenResponse> verifyOtp(@Valid @RequestBody VerifyOtpDTO request,
                                            HttpServletResponse response);

    @Operation(summary = "Login with email + password (account must be verified)")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials or unverified account")
    @PostMapping("/login")
    ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginDTO loginRequest,
                                        HttpServletResponse response);

    @Operation(summary = "Refresh access token using refresh token")
    @ApiResponse(responseCode = "200", description = "Token refreshed")
    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    @PostMapping("/refresh")
    ResponseEntity<TokenResponse> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest body,
            HttpServletResponse response,
            HttpServletRequest request);

    @Operation(summary = "Logout and revoke refresh token")
    @ApiResponse(responseCode = "204", description = "Logged out")
    @PostMapping("/logout")
    ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response);
}
