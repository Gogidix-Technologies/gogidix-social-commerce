package com.gogidix.socialcommerce.analytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Object, Long> {
    // Placeholder - actual Payment entity would come from payment-service
}
