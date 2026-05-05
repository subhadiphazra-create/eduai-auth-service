package com.eduai.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a role operation is not permitted by business rules
 * (e.g., attempting to rename a system role, recreate an existing system role, etc.).
 *
 * Maps to HTTP 422 Unprocessable Entity — the request was well-formed
 * but failed a business rule, not a validation rule.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class RoleOperationException extends RuntimeException {

    public RoleOperationException(String message) {
        super(message);
    }
}
