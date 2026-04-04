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
    // GET OR CREATE (🔥 FIXED)
    // ===============================
    public Subscription getOrCreate(Long customerId) {

        Subscription s = repo.findByCustomerId(customerId)
            .orElseGet(() -> {
                Subscription newSub = new Subscription();

                newSub.setCustomerId(customerId);
                newSub.setStatus("ACTIVE");
                newSub.setQtyLitres(1);
                newSub.setNextDeliveryDate(LocalDate.now());
                newSub.setEndDate(LocalDate.now().plusDays(30));

                return newSub;
            });

        // 🔥 CRITICAL FIX: ALWAYS SET VENDOR ID
        if (s.getVendorId() == null) {
            s.setVendorId(24L); // your vendor ID
        }

        return repo.save(s);
    }

    // ===============================
    // GET SUBSCRIPTION
    // ===============================
    public Subscription get(Long customerId) {
        Subscription s = getOrCreate(customerId);

        LocalDate today = LocalDate.now();

        if (s.getStatus().equals("PAUSED") &&
                s.getNextDeliveryDate() != null &&
                !today.isBefore(s.getNextDeliveryDate())) {

            s.setStatus("ACTIVE");
        }

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
        s.setStatus("PAUSED");

        if (!startDate.isAfter(today)) {
            s.setNextDeliveryDate(endDate.plusDays(1));
        } else {
            s.setNextDeliveryDate(startDate);
        }

        log(customerId, "PAUSE");

        return repo.save(s);
    }

    // ===============================
    // RESUME
    // ===============================
    public Subscription resume(Long customerId) {
        Subscription s = getOrCreate(customerId);

        List<PausePeriod> pauses = s.getPauses();

        if (pauses != null && !pauses.isEmpty()) {

            long pausedDays = 0;

            for (PausePeriod p : pauses) {
                long days = ChronoUnit.DAYS.between(p.getStartDate(), p.getEndDate()) + 1;
                pausedDays += days;
            }

            if (pausedDays > 0) {
                s.setEndDate(s.getEndDate().plusDays(pausedDays));
            }

            s.getPauses().clear();
        }

        s.setStatus("ACTIVE");

        log(customerId, "RESUME");

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
    // GET ALL SUBSCRIPTIONS BY VENDOR
    // ===============================
    public List<Subscription> getByVendorId(Long vendorId) {
        return repo.findByVendorId(vendorId);
    }

    // ===============================
    // PAUSE SUGGESTION (VENDOR)
    // ===============================
    public String getPauseSuggestion(Long vendorId) {

        List<Subscription> subs = repo.findByVendorId(vendorId);

        if (subs == null || subs.isEmpty()) {
            return "No customers available";
        }

        long pausedCount = subs.stream()
                .filter(s -> "PAUSED".equalsIgnoreCase(s.getStatus()))
                .count();

        if (pausedCount == 0) {
            return "No pause pattern found";
        }

        if (pausedCount > subs.size() / 2) {
            return "⚠️ Many customers paused — check supply or pricing";
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