package com.eduai.auth.repository.role;

import com.eduai.auth.entity.role.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

    /** Find all roles for a specific app. */
    List<RoleEntity> findAllByAppId(String appId);

    /** Find a specific role by app + name (case-sensitive). */
    Optional<RoleEntity> findByAppIdAndRoleName(String appId, String roleName);

    /** Check whether a role already exists in an app (for uniqueness validation). */
    boolean existsByAppIdAndRoleName(String appId, String roleName);

    /** Delete all roles belonging to a specific app (used when deactivating/removing an app). */
    void deleteAllByAppId(String appId);

    /**
     * Bulk rename: when a role is renamed, update all existing user_roles entries.
     * This keeps UserEntity.roles (stored as strings) consistent after a rename.
     */
    @Modifying
    @Query(value = "UPDATE user_roles SET role = :newName WHERE role = :oldName AND user_id IN " +
                   "(SELECT id FROM auth_users WHERE app_id = :appId)", nativeQuery = true)
    int bulkRenameUserRole(String appId, String oldName, String newName);
}
