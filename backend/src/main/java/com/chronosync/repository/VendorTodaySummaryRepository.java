package com.chronosync.repository;

import com.chronosync.model.VendorTodaySummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface VendorTodaySummaryRepository extends JpaRepository<VendorTodaySummary, Long> {
    Optional<VendorTodaySummary> findByVendorIdAndSummaryDate(Long vendorId, LocalDate summaryDate);
}