package com.chronosync.service;

import com.chronosync.dto.VendorAnalyticsDTO;
import com.chronosync.model.Subscription;
import com.chronosync.model.VendorAnalyticsSnapshot;
import com.chronosync.repository.SubscriptionRepository;
import com.chronosync.repository.VendorAnalyticsSnapshotRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VendorSchedulerService {

    private final SubscriptionRepository subRepo;
    private final VendorService vendorService;
    private final VendorAnalyticsSnapshotRepository snapRepo;

    public VendorSchedulerService(SubscriptionRepository subRepo,
                                  VendorService vendorService,
                                  VendorAnalyticsSnapshotRepository snapRepo) {
        this.subRepo = subRepo;
        this.vendorService = vendorService;
        this.snapRepo = snapRepo;
    }

    // ⏰ Runs every day at 5:00 AM
    @Scheduled(cron = "0 0 5 * * *")
    public void dailyPrecompute() {

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // Get all unique vendorIds
        List<Long> vendorIds = subRepo.findAll().stream()
                .map(Subscription::getVendorId)
                .distinct()
                .toList();

        for (Long vendorId : vendorIds) {

            // Use your live analytics logic
            VendorAnalyticsDTO dto =
                    vendorService.getAnalytics(vendorId, "monthToDate");

            VendorAnalyticsSnapshot snapshot =
                    new VendorAnalyticsSnapshot(
                            vendorId,
                            "monthToDate",
                            today,
                            dto.getDeliveredMilkL(),
                            dto.getMilkSavedL(),
                            now
                    );

            snapRepo.save(snapshot);
        }

        System.out.println("✅ Vendor analytics precomputed at " + now);
    }
}