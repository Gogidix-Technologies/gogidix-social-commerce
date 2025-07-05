package com.gogidix.socialcommerce.analytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Object, Long> {
    // Placeholder - actual User entity would come from user-service
}
