package com.exalt.socialcommerce.analytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Object, Long> {
    // Placeholder - actual Order entity would come from order-service
}
