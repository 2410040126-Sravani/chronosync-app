package com.chronosync.repository;

import com.chronosync.model.VendorAnalyticsSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface VendorAnalyticsSnapshotRepository extends JpaRepository<VendorAnalyticsSnapshot, Long> {

    Optional<VendorAnalyticsSnapshot> findTopByVendorIdAndWindowKeyAndSnapshotDateOrderByComputedAtDesc(
            Long vendorId, String windowKey, LocalDate snapshotDate
    );
}