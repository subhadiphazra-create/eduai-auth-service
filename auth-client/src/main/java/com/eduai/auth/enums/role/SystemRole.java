package com.eduai.auth.enums.role;

/**
 * System-level roles that every app gets by default.
 * These are seed roles — apps can define additional custom roles on top.
 *
 * Hierarchy (highest to lowest):
 *   SUPER_ADMIN → ADMIN → CLIENT
 */
public enum SystemRole {

    /** Platform-wide super administrator. Can manage apps, promote/demote admins. */
    SUPER_ADMIN,

    /** Application-level administrator. Can manage users and roles within their app. */
    ADMIN,

    /** Default role assigned to every newly registered user. */
    CLIENT;

    public String value() {
        return this.name();
    }

    /**
     * Returns the privilege level. Higher = more powerful.
     */
    public int privilegeLevel() {
        return switch (this) {
            case SUPER_ADMIN -> 100;
            case ADMIN       -> 50;
            case CLIENT      -> 10;
        };
    }

    /**
     * Returns whether {@code other} can be promoted/demoted by this role.
     */
    public boolean canManage(SystemRole other) {
        return this.privilegeLevel() > other.privilegeLevel();
    }
}
