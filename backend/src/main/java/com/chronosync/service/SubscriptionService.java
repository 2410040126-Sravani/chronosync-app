package com.chronosync.service;

import com.chronosync.model.Subscription;
import com.chronosync.model.PausePeriod;
import com.chronosync.model.AuditLog;
import com.chronosync.repository.SubscriptionRepository;
import com.chronosync.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class SubscriptionService {

    private final SubscriptionRepository repo;
    private final AuditLogRepository auditRepo;

    public SubscriptionService(SubscriptionRepository repo,
                               AuditLogRepository auditRepo) {
        this.repo = repo;
        this.auditRepo = auditRepo;
    }

    // ===============================
    // GET OR CREATE
    // ===============================
    public Subscription getOrCreate(Long customerId) {
        return repo.findByCustomerId(customerId)
            .orElseGet(() -> {
                Subscription s = new Subscription();

                s.setCustomerId(customerId);
                s.setVendorId(24L);

                s.setStatus("ACTIVE");
                s.setQtyLitres(1);
                s.setNextDeliveryDate(LocalDate.now());
                s.setEndDate(LocalDate.now().plusDays(30));

                return repo.save(s);
            });
    }

    // ===============================
    // GET SUBSCRIPTION
    // ===============================
    public Subscription get(Long customerId) {
        Subscription s = getOrCreate(customerId);

        LocalDate today = LocalDate.now();

        if (s.getEndDate() == null) {
            s.setEndDate(today.plusDays(30));
        }

        // Auto resume after pause ends
        if ("PAUSED".equals(s.getStatus()) &&
                s.getNextDeliveryDate() != null &&
                !today.isBefore(s.getNextDeliveryDate())) {

            s.setStatus("ACTIVE");
        }

        // 🔥 FIXED EFFECTIVE END DATE
        s.setEffectiveEndDate(calculateEffectiveEndDate(s));

        return repo.save(s);
    }

    // ===============================
    // UPDATE QUANTITY
    // ===============================
    public Subscription updateQty(Long customerId, int value) {
        Subscription s = getOrCreate(customerId);
        s.setQtyLitres(value);
        return repo.save(s);
    }

    // ===============================
    // PAUSE
    // ===============================
    public Subscription pause(Long customerId, String start, String end) {

        Subscription s = getOrCreate(customerId);

        if (s.getPauses() == null) {
            s.setPauses(new ArrayList<>());
        }

        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        LocalDate today = LocalDate.now();

        if (startDate.isBefore(today.minusDays(1))) {
            throw new RuntimeException("Start date cannot be in past");
        }

        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End must be after start");
        }

        PausePeriod p = new PausePeriod();
        p.setStartDate(startDate);
        p.setEndDate(endDate);
        p.setSubscription(s);

        s.getPauses().add(p);

        if (!today.isBefore(startDate) && !today.isAfter(endDate)) {
            s.setStatus("PAUSED");
        }

        s.setNextDeliveryDate(endDate.plusDays(1));

        log(customerId, "PAUSE");

        return repo.save(s);
    }

    // ===============================
    // RESUME (DEMO SIMPLE)
    // ===============================
    public Subscription resume(Long customerId) {
        Subscription s = getOrCreate(customerId);

        if (s.getPauses() != null) {
            s.getPauses().clear();
        }

        s.setStatus("ACTIVE");

        log(customerId, "RESUME");

        return repo.save(s);
    }

    // ===============================
    // CLEAR PAUSES
    // ===============================
    public Subscription clearPauses(Long customerId) {
        Subscription s = getOrCreate(customerId);

        if (s.getPauses() != null) {
            s.getPauses().clear();
        }

        s.setStatus("ACTIVE");

        log(customerId, "CLEAR_PAUSES");

        return repo.save(s);
    }

    // ===============================
    // EXTEND
    // ===============================
    public Subscription extend(Long customerId, int days) {
        Subscription s = getOrCreate(customerId);

        if (s.getEndDate() != null) {
            s.setEndDate(s.getEndDate().plusDays(days));
        } else {
            s.setEndDate(LocalDate.now().plusDays(days));
        }

        return repo.save(s);
    }

    // ===============================
    // 🔥 FIXED EFFECTIVE END DATE
    // ===============================
    private LocalDate calculateEffectiveEndDate(Subscription s) {

        LocalDate today = LocalDate.now();

        if (s.getEndDate() == null) {
            return today.plusDays(30);
        }

        if (s.getPauses() == null || s.getPauses().isEmpty()) {
            return s.getEndDate();
        }

        long totalPauseDays = 0;

        for (PausePeriod p : s.getPauses()) {

            if (p.getStartDate() != null && p.getEndDate() != null) {

                LocalDate start = p.getStartDate();
                LocalDate end = p.getEndDate();

                // 🔥 FIX: count ongoing pause also
                if (end.isAfter(today)) {
                    end = today;
                }

                if (!start.isAfter(today)) {
                    long days = ChronoUnit.DAYS.between(start, end) + 1;
                    totalPauseDays += days;
                }
            }
        }

        return s.getEndDate().plusDays(totalPauseDays);
    }

    // ===============================
    // GET BY VENDOR
    // ===============================
    public List<Subscription> getByVendorId(Long vendorId) {
        return repo.findByVendorId(vendorId);
    }

    // ===============================
    // PAUSE SUGGESTION
    // ===============================
    public String getPauseSuggestion(Long vendorId) {

        List<Subscription> subs = repo.findByVendorId(vendorId);

        if (subs == null || subs.isEmpty()) {
            return "No customers available";
        }

        long paused = subs.stream()
                .filter(s -> "PAUSED".equalsIgnoreCase(s.getStatus()))
                .count();

        if (paused == 0) {
            return "No pause activity";
        }

        if (paused > subs.size() / 2) {
            return "⚠️ Many customers paused — check service quality";
        }

        return "Normal pause activity";
    }

    // ===============================
    // LOGGER
    // ===============================
    private void log(Long customerId, String action) {
        AuditLog log = new AuditLog();
        log.setCustomerId(customerId);
        log.setAction(action);
        log.setTimestamp(LocalDateTime.now());

        auditRepo.save(log);
    }
}