package com.eduai.auth.service.user;

import com.eduai.auth.dto.request.user.ChangePasswordDTO;
import com.eduai.auth.dto.request.user.RegisterDTO;
import com.eduai.auth.dto.request.user.VerifyOtpDTO;
import com.eduai.auth.dto.request.token.TokenResponse;
import com.eduai.auth.dto.response.user.UserResponseDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    /** Register a new user. Sends OTP — account is NOT active until OTP verified. */
    UserResponseDTO register(RegisterDTO dto);

    /** Verify OTP — activates the account and returns a token pair. */
    TokenResponse verifyOtp(VerifyOtpDTO dto, HttpServletResponse response);

    UserResponseDTO changePassword(ChangePasswordDTO dto);

    UserResponseDTO getUserByEmail(String email, String appId);

    UserResponseDTO softDeleteUser(String email, String appId);

    Page<UserResponseDTO> getAllUsers(String appId, Pageable pageable);
}
