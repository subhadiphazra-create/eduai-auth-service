package com.eduai.auth.mapper.role;

import com.eduai.auth.dto.request.role.CreateRoleRequest;
import com.eduai.auth.dto.response.role.RoleResponse;
import com.eduai.auth.entity.role.RoleEntity;
import com.eduai.auth.mapper.GenericMapper;
import org.springframework.stereotype.Component;

/**
 * Maps between RoleEntity and its DTOs.
 * Implements GenericMapper for the standard pattern.
 */
@Component
public class RoleMapper implements GenericMapper<RoleEntity, CreateRoleRequest, RoleResponse> {

    @Override
    public RoleEntity toEntity(CreateRoleRequest request) {
        if (request == null) return null;
        return RoleEntity.builder()
                .appId(request.appId())
                .roleName(request.roleName().toUpperCase())
                .description(request.description())
                .systemRole(false)
                .build();
    }

    @Override
    public RoleResponse toResponse(RoleEntity entity) {
        if (entity == null) return null;
        return RoleResponse.builder()
                .id(entity.getId())
                .appId(entity.getAppId())
                .roleName(entity.getRoleName())
                .description(entity.getDescription())
                .systemRole(entity.isSystemRole())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
