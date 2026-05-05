package com.eduai.auth.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    /**
     * App identifier — auth-service resolves the correct app context.
     * Must match an AppRegistration configured in the service.
     */
    @NotBlank(message = "appId is required")
    private String appId;
}
