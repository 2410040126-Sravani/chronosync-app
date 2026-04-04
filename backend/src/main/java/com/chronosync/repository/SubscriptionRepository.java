package com.chronosync.repository;

import com.chronosync.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // 🔥 for customer dashboard
    Optional<Subscription> findByCustomerId(Long customerId);

    // 🔥 for vendor dashboard
    List<Subscription> findByVendorId(Long vendorId);

}