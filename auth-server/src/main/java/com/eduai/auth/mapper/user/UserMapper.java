package com.eduai.auth.mapper.user;

import com.eduai.auth.dto.response.user.UserResponseDTO;
import com.eduai.auth.entity.user.UserEntity;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserMapper {

    public UserResponseDTO toResponse(UserEntity entity) {
        if (entity == null) return null;
        return UserResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .appId(entity.getAppId())
                .roles(entity.getRoles() != null ? Set.copyOf(entity.getRoles()) : Set.of())
                .provider(entity.getProvider())
                .image(entity.getImage())
                .enabled(entity.isEnabled())
                .emailVerified(Boolean.TRUE.equals(entity.getEmailVerified()))
                .build();
    }
}
