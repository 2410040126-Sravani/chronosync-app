package com.chronosync.service;

import com.chronosync.model.Subscription;
import com.chronosync.model.VendorTodaySummary;
import com.chronosync.repository.SubscriptionRepository;
import com.chronosync.repository.VendorTodaySummaryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VendorPrecomputeService {

    private final SubscriptionRepository subRepo;
    private final VendorTodaySummaryRepository summaryRepo;

    public VendorPrecomputeService(SubscriptionRepository subRepo,
                                   VendorTodaySummaryRepository summaryRepo) {
        this.subRepo = subRepo;
        this.summaryRepo = summaryRepo;
    }

    // ✅ Call this whenever vendor presses "Recompute"
    public VendorTodaySummary recomputeForVendor(Long vendorId) {

        LocalDate today = LocalDate.now();
        VendorTodaySummary summary = summaryRepo
                .findByVendorIdAndSummaryDate(vendorId, today)
                .orElseGet(() -> new VendorTodaySummary(vendorId, today));

        List<Subscription> subs = subRepo.findByVendorId(vendorId);

        int stops = subs.size();
        int paused = (int) subs.stream().filter(s -> "PAUSED".equalsIgnoreCase(s.getStatus())).count();
        int totalLitres = subs.stream()
                .filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus()))
                .mapToInt(Subscription::getQtyLitres)
                .sum();

        summary.setStops(stops);
        summary.setPausedCount(paused);
        summary.setTotalLitres(totalLitres);
        summary.setGeneratedAt(LocalDateTime.now().toString());

        return summaryRepo.save(summary);
    }

    // ✅ Scheduler-based precomputation (demo-friendly)
    // Runs every day at 5:00 AM
    @Scheduled(cron = "0 0 5 * * *")
    public void dailyPrecompute() {
        // for demo: precompute vendor 1
        recomputeForVendor(1L);
    }
}
