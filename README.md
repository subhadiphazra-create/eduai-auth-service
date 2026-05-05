# auth-service — Enhanced with Role Management System

## What's New & What Was Fixed

### 🐛 Bug Fixes (from original codebase)

| File | Bug | Fix |
|------|-----|-----|
| `AppRegistrationResponse` | Missing `@Getter` — Jackson couldn't serialize fields | Added `@Getter`, `@Setter`, `@Builder` |
| `AppRegistrationController` | Returned raw `AppRegistration` entity instead of DTO | Now uses `AppRegistrationMapper` |
| `AppRegistrationController` | `activate()` did a full table scan | Added `findByAppId()` to repo |
| `AppRegistrationController` | `register/deactivate/activate` missing `@PreAuthorize` | All endpoints now secured with `SUPER_ADMIN` |
| `UserServiceImpl.resolveAppIdFromContext()` | Did a full `findAll()` scan to find appId | Now uses `findByEmail()` from security context |
| `GlobalExceptionHandler` | `AccessDeniedException` not handled — returned 500 | Added handler → 403 |
| `JwtAuthenticationFilter` | Already present in original (bugs were already fixed in provided zip) | Kept as-is |

---

## Architecture

```
auth-service/
├── auth-client/                    # Shared module (DTOs, API interfaces)
│   └── src/main/java/com/eduai/auth/
│       ├── api/
│       │   ├── app/AppRegistrationAPI.java
│       │   ├── auth/AuthAPI.java
│       │   ├── role/RoleAPI.java          ← NEW
│       │   └── user/UserAPI.java
│       ├── dto/
│       │   ├── request/role/              ← NEW (4 request DTOs)
│       │   └── response/role/             ← NEW (2 response DTOs)
│       └── enums/role/SystemRole.java     ← NEW
│
└── auth-server/                    # Implementation module
    └── src/main/java/com/eduai/auth/
        ├── config/SecurityConfig.java     ← UPDATED
        ├── controller/
        │   ├── app/AppRegistrationController.java  ← FIXED
        │   ├── auth/AuthController.java
        │   ├── role/RoleController.java   ← NEW
        │   └── user/UserController.java
        ├── entity/role/RoleEntity.java    ← NEW
        ├── exception/
        │   ├── GlobalExceptionHandler.java ← UPDATED
        │   └── RoleOperationException.java ← NEW
        ├── mapper/
        │   ├── GenericMapper.java         ← NEW (generic interface)
        │   ├── app/AppRegistrationMapper.java ← NEW
        │   ├── role/RoleMapper.java       ← NEW
        │   └── user/UserMapper.java
        ├── repository/role/RoleRepository.java ← NEW
        ├── security/SecurityContextHelper.java  ← NEW
        └── service/
            ├── impl/role/RoleServiceImpl.java   ← NEW
            ├── impl/user/UserServiceImpl.java   ← UPDATED (CLIENT role on register)
            └── role/
                ├── AccessControlService.java    ← NEW
                └── RoleService.java             ← NEW
```

---

## Role Management System

### Role Hierarchy

```
SUPER_ADMIN (100)   ← Can manage everything, promote/demote anyone
     ↓
ADMIN (50)          ← Can manage users and roles within their app
     ↓
CLIENT (10)         ← Default role for all newly registered users
     ↓
[Custom Roles]      ← App-specific roles (TEACHER, HR_MANAGER, etc.)
```

### System Roles
When a new app is registered via `POST /api/v1/admin/apps`, three system roles are automatically seeded:
- `SUPER_ADMIN` — Cannot be renamed or deleted
- `ADMIN` — Cannot be renamed or deleted  
- `CLIENT` — Cannot be renamed or deleted

### Default Role on Registration
Every user who registers via `POST /api/v1/auth/register` is automatically assigned the `CLIENT` role.

---

## API Reference

### Role APIs (`/api/v1/roles`)

| Method | Path | Auth Required | Description |
|--------|------|---------------|-------------|
| `GET` | `/api/v1/roles/my-role` | Any authenticated user | Get current user's roles |
| `GET` | `/api/v1/roles/{appId}` | ADMIN or SUPER_ADMIN | List all roles for an app |
| `POST` | `/api/v1/roles` | SUPER_ADMIN | Create a new custom role |
| `PUT` | `/api/v1/roles/{appId}/{roleName}` | SUPER_ADMIN | Update role description |
| `PATCH` | `/api/v1/roles/{appId}/{roleName}/rename` | SUPER_ADMIN | Rename a role (not system roles) |
| `POST` | `/api/v1/roles/{appId}/assign` | ADMIN or SUPER_ADMIN | Assign a role to a user |

### App Admin APIs (`/api/v1/admin/apps`)

| Method | Path | Auth Required | Description |
|--------|------|---------------|-------------|
| `GET` | `/api/v1/admin/apps` | SUPER_ADMIN | List all registered apps |
| `POST` | `/api/v1/admin/apps` | SUPER_ADMIN | Register a new app (seeds system roles) |
| `PATCH` | `/api/v1/admin/apps/{appId}/deactivate` | SUPER_ADMIN | Deactivate an app |
| `PATCH` | `/api/v1/admin/apps/{appId}/activate` | SUPER_ADMIN | Re-activate an app |

---

## Security Rules

### Role Assignment Rules
- **SUPER_ADMIN** → Can assign any role to any user (except themselves)
- **ADMIN** → Can assign roles up to ADMIN level only; cannot promote to SUPER_ADMIN
- **No self-promotion** → A user cannot change their own role
- **Hierarchy enforcement** → Cannot manage a user with equal or higher privilege

### Role Naming
- Must be `UPPER_SNAKE_CASE`: `TEACHER`, `HR_MANAGER`, `CONTENT_CREATOR`
- 2–50 characters
- Cannot duplicate or overwrite system roles

---

## AccessControlService (for other microservices)

The `AccessControlService` is the centralized authorization oracle. Other services should call it rather than implementing their own role checks:

```java
@Autowired AccessControlService acl;

// Check if a user is admin or above in a specific app
boolean canModify = acl.isAdminOrAbove(userId, appId);

// Check for a specific custom role
boolean isTeacher = acl.hasAnyRole(userId, appId, "TEACHER");

// Validate a role name before using it
boolean roleExists = acl.isValidRoleInApp(appId, "TEACHER");
```

---

## Database Schema (new tables)

```sql
-- roles table
CREATE TABLE roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    app_id      VARCHAR NOT NULL,
    role_name   VARCHAR NOT NULL,
    description VARCHAR(500),
    system_role BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    UNIQUE (app_id, role_name)
);

-- user_roles is pre-existing (ElementCollection on UserEntity)
-- No changes needed to user_roles table schema
```

---

## Configuration

No new configuration properties are required. All existing properties in `application.yml` / `application.properties` remain compatible.

---

## Building

```bash
# Install auth-client module first (dependency for auth-server)
cd auth-client && mvn install -DskipTests

# Build auth-server
cd ../auth-server && mvn package -DskipTests
```
