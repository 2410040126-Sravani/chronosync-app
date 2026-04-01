package com.chronosync.repository;

import com.chronosync.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByCustomerId(Long customerId);

    // ✅ ADD THIS LINE (THIS IS YOUR ERROR FIX)
    List<Subscription> findByVendorId(Long vendorId);
}