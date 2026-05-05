package com.eduai.auth.api.user;

import com.eduai.auth.dto.request.user.ChangePasswordDTO;
import com.eduai.auth.dto.response.user.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/users")
public interface UserAPI {

    @Operation(summary = "Change password for the currently authenticated user")
    @ApiResponse(responseCode = "200", description = "Password changed successfully")
    @ApiResponse(responseCode = "400", description = "Validation error or wrong current password")
    @ApiResponse(responseCode = "401", description = "Unauthenticated")
    @PatchMapping("/change-password")
    ResponseEntity<UserResponseDTO> changePassword(@Valid @RequestBody ChangePasswordDTO dto);

    @Operation(summary = "Get a user by email within a specific app — admin only")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "403", description = "Insufficient authority")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{appId}/{email}")
    ResponseEntity<UserResponseDTO> getUser(@PathVariable String appId,
                                            @PathVariable String email);

    @Operation(summary = "List all active users for an app — paginated, admin only")
    @ApiResponse(responseCode = "200", description = "Page of users returned")
    @ApiResponse(responseCode = "403", description = "Insufficient authority")
    @GetMapping("/{appId}")
    ResponseEntity<Page<UserResponseDTO>> getAllUsers(@PathVariable String appId,
                                                      Pageable pageable);

    @Operation(summary = "Soft-delete a user and revoke all their refresh tokens — admin only")
    @ApiResponse(responseCode = "200", description = "User deleted")
    @ApiResponse(responseCode = "403", description = "Insufficient authority")
    @ApiResponse(responseCode = "404", description = "User not found")
    @DeleteMapping("/{appId}/{email}")
    ResponseEntity<UserResponseDTO> deleteUser(@PathVariable String appId,
                                               @PathVariable String email);
}