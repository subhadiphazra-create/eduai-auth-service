package com.eduai.auth.controller.user;

import com.eduai.auth.api.user.UserAPI;
import com.eduai.auth.dto.request.user.ChangePasswordDTO;
import com.eduai.auth.dto.response.user.UserResponseDTO;
import com.eduai.auth.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController implements UserAPI {

    private final UserService userService;

    @Override
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<UserResponseDTO> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        return ResponseEntity.ok(userService.changePassword(dto));
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable String appId,
                                                   @PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email, appId));
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(@PathVariable String appId,
                                                             Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(appId, pageable));
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<UserResponseDTO> deleteUser(@PathVariable String appId,
                                                      @PathVariable String email) {
        return ResponseEntity.ok(userService.softDeleteUser(email, appId));
    }
}