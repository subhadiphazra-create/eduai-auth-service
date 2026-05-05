package com.eduai.auth.api.app;

import com.eduai.auth.dto.request.app.AppRegistrationRequest;
import com.eduai.auth.dto.response.app.AppRegistrationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/admin/apps")
public interface AppRegistrationAPI {

    @Operation(summary = "List all registered applications — super admin only")
    @ApiResponse(responseCode = "200", description = "List returned")
    @ApiResponse(responseCode = "403", description = "Insufficient authority")
    @GetMapping
    ResponseEntity<List<AppRegistrationResponse>> listAll();

    @Operation(summary = "Register a new application — super admin only")
    @ApiResponse(responseCode = "201", description = "App registered")
    @ApiResponse(responseCode = "400", description = "Validation error or appId already exists")
    @ApiResponse(responseCode = "403", description = "Insufficient authority")
    @PostMapping
    ResponseEntity<AppRegistrationResponse> register(@Valid @RequestBody AppRegistrationRequest req);

    @Operation(summary = "Deactivate an active application — super admin only")
    @ApiResponse(responseCode = "200", description = "App deactivated")
    @ApiResponse(responseCode = "403", description = "Insufficient authority")
    @ApiResponse(responseCode = "404", description = "App not found or already inactive")
    @PatchMapping("/{appId}/deactivate")
    ResponseEntity<AppRegistrationResponse> deactivate(@PathVariable String appId);

    @Operation(summary = "Reactivate an inactive application — super admin only")
    @ApiResponse(responseCode = "200", description = "App activated")
    @ApiResponse(responseCode = "403", description = "Insufficient authority")
    @ApiResponse(responseCode = "404", description = "App not found")
    @PatchMapping("/{appId}/activate")
    ResponseEntity<AppRegistrationResponse> activate(@PathVariable String appId);
}