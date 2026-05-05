package com.eduai.auth.dto.response.user;

import com.eduai.auth.enums.user.ProviderENUM;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDTO {
    private UUID id;
    private String name;
    private String email;
    private String appId;
    private Set<String> roles;
    private ProviderENUM provider;
    private String image;
    private boolean enabled;
    private boolean emailVerified;
    private String message;
}
