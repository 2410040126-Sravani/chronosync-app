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
import java.time.format.DateTimeFormatter;
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
                s.setVendorId(1L);
                s.setStatus("ACTIVE");
                s.setQtyLitres(1);
                s.setNextDeliveryDate(LocalDate.now());
                return repo.save(s);
            });
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

        int oldQty = s.getQtyLitres();
        s.setQtyLitres(value);

        System.out.println("[AUDIT] Customer " + customerId +
                " → QTY CHANGE: " + oldQty + "L → " + value + "L");

        return repo.save(s);
    }

    // ===============================
    // VALIDATION
    // ===============================
    public void validatePauseAllowed(Subscription subscription, LocalDate startDate) {
        LocalDate today = LocalDate.now();

        if (subscription.getNextDeliveryDate() != null &&
        	    startDate.isEqual(subscription.getNextDeliveryDate())) {
        	    
        	    // instead of crashing → just move to next day
        	    startDate = startDate.plusDays(1);
        	}

        if (subscription.getEndDate() != null &&
            today.isAfter(subscription.getEndDate())) {
            throw new RuntimeException("Subscription already ended");
        }
    }

    // ===============================
    // PAUSE
    // ===============================
    public Subscription pause(Long customerId, String start, String end) {

        Subscription s = getOrCreate(customerId);
        if (s.getPauses() == null) {
            s.setPauses(new java.util.ArrayList<>());
        }

        // ✅ FIXED DATE PARSING
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        LocalDate today = LocalDate.now();

        System.out.println("START: " + startDate);
        System.out.println("END: " + endDate);
        System.out.println("TODAY: " + today);

        validatePauseAllowed(s, startDate);

        if (startDate.isBefore(today.minusDays(1))) {
            throw new IllegalArgumentException("Start date cannot be in past");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End must be after start");
        }

        if (s.getPauses() == null) {
            s.setPauses(new ArrayList<>());
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

        log(customerId, "PAUSE (" + start + " → " + end + ")");

        return repo.save(s);
    }

    // ===============================
    // RESUME
    // ===============================
    public Subscription resume(Long customerId) {
        Subscription s = getOrCreate(customerId);

        List<PausePeriod> pauses = s.getPauses();
        LocalDate today = LocalDate.now();

        if (pauses != null && !pauses.isEmpty()) {

            long actualPausedDays = 0;

            for (PausePeriod p : pauses) {

                LocalDate start = p.getStartDate();
                LocalDate end = p.getEndDate();

                if (end.isBefore(today) || end.isEqual(today)) {
                    long days = ChronoUnit.DAYS.between(start, end) + 1;
                    actualPausedDays += days;
                }
            }

            if (actualPausedDays > 0) {
                if (s.getEndDate() == null) {
                    s.setEndDate(today);
                }

                s.setEndDate(s.getEndDate().plusDays(actualPausedDays));
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

        if (s.getEndDate() == null) {
            s.setEndDate(LocalDate.now());
        }

        s.setEndDate(s.getEndDate().plusDays(days));

        log(customerId, "EXTEND → " + days + " days");

        return repo.save(s);
    }

    // ===============================
    // SUGGESTION
    // ===============================
    public String getPauseSuggestion(Long customerId) {
        Subscription s = getOrCreate(customerId);

        LocalDate today = LocalDate.now();
        LocalDate next = s.getNextDeliveryDate();

        if (next != null && ChronoUnit.DAYS.between(today, next) <= 2) {
            return "You may pause upcoming deliveries";
        }

        return "No pause needed";
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