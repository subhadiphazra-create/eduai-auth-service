package com.eduai.auth.controller.app;

import com.eduai.auth.api.app.AppRegistrationAPI;
import com.eduai.auth.dto.request.app.AppRegistrationRequest;
import com.eduai.auth.dto.response.app.AppRegistrationResponse;
import com.eduai.auth.entity.app.AppRegistration;
import com.eduai.auth.exception.ResourceAlreadyExistsException;
import com.eduai.auth.exception.ResourceNotFoundException;
import com.eduai.auth.mapper.app.AppRegistrationMapper;
import com.eduai.auth.repository.app.AppRegistrationRepository;
import com.eduai.auth.service.role.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin-only controller for managing registered applications.
 *
 * <p>Fixes from original:
 * <ul>
 *   <li>Was returning raw {@code AppRegistration} entities — now uses {@link AppRegistrationMapper}
 *       to return proper {@link AppRegistrationResponse} DTOs.</li>
 *   <li>Was missing {@code @PreAuthorize} on register/deactivate/activate — all now protected.</li>
 *   <li>activate() was doing a full table scan — now uses a proper repository query.</li>
 *   <li>Injected {@link RoleService} to seed system roles when a new app is registered.</li>
 * </ul>
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AppRegistrationController implements AppRegistrationAPI {

    private final AppRegistrationRepository appRegistrationRepository;
    private final AppRegistrationMapper     appRegistrationMapper;
    private final RoleService               roleService;

    @Override
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<List<AppRegistrationResponse>> listAll() {
        List<AppRegistrationResponse> list = appRegistrationRepository.findAll()
                .stream()
                .map(appRegistrationMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @Override
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<AppRegistrationResponse> register(@Valid AppRegistrationRequest req) {
        if (appRegistrationRepository.existsByAppId(req.appId())) {
            throw new ResourceAlreadyExistsException("App '" + req.appId() + "' is already registered.");
        }
        AppRegistration app = appRegistrationMapper.toEntity(req);
        AppRegistration saved = appRegistrationRepository.save(app);

        // Seed system roles (SUPER_ADMIN, ADMIN, CLIENT) for this newly registered app
        roleService.seedSystemRoles(saved.getAppId(), saved.getAppName());
        log.info("Registered new app '{}' and seeded system roles.", saved.getAppId());

        return ResponseEntity.status(HttpStatus.CREATED).body(appRegistrationMapper.toResponse(saved));
    }

    @Override
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<AppRegistrationResponse> deactivate(String appId) {
        AppRegistration app = appRegistrationRepository.findByAppIdAndActiveTrue(appId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "App not found or already inactive: " + appId));
        app.setActive(false);
        return ResponseEntity.ok(appRegistrationMapper.toResponse(appRegistrationRepository.save(app)));
    }

    @Override
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<AppRegistrationResponse> activate(String appId) {
        // FIX: original did a full table scan; now uses a dedicated query
        AppRegistration app = appRegistrationRepository.findByAppId(appId)
                .orElseThrow(() -> new ResourceNotFoundException("App not found: " + appId));
        app.setActive(true);
        return ResponseEntity.ok(appRegistrationMapper.toResponse(appRegistrationRepository.save(app)));
    }
}
