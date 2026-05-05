package com.eduai.auth.repository.app;

import com.eduai.auth.entity.app.AppRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppRegistrationRepository extends JpaRepository<AppRegistration, UUID> {

    Optional<AppRegistration> findByAppIdAndActiveTrue(String appId);

    /** Fixed: activate() was doing a full table scan. Now uses this query. */
    Optional<AppRegistration> findByAppId(String appId);

    boolean existsByAppId(String appId);
}
