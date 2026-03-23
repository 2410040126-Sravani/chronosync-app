package com.chronosync.service;

import com.chronosync.dto.PauseSuggestionDTO;
import com.chronosync.model.AuditLog;
import com.chronosync.model.Subscription;
import com.chronosync.repository.AuditLogRepository;
import com.chronosync.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class SubscriptionService {

    private final SubscriptionRepository repo;
    private final AuditLogRepository auditRepo;

    public SubscriptionService(SubscriptionRepository repo, AuditLogRepository auditRepo) {
        this.repo = repo;
        this.auditRepo = auditRepo;
    }

    /* ---------------- AUDIT HELPER ---------------- */
    private void log(Long customerId, String action) {
        auditRepo.save(new AuditLog(customerId, action));
    }

    /* ---------------- CORE ---------------- */
    private Subscription getOrCreate(Long customerId) {
        return repo.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Subscription s = new Subscription();
                    s.setCustomerId(customerId);

                    if (s.getStatus() == null) s.setStatus("ACTIVE");
                    if (s.getQtyLitres() == 0) s.setQtyLitres(1);

                    if (s.getVendorId() == null) s.setVendorId(1L);

                    if (s.getEndDate() == null) s.setEndDate(LocalDate.now().plusDays(30));

                    return repo.save(s);
                });
    }

    public Subscription getSubscription(Long customerId) {
        return getOrCreate(customerId);
    }

    /* ---------------- ACTIONS ---------------- */
    public Subscription updateQty(Long customerId, int qtyLitres) {
        Subscription s = getOrCreate(customerId);
        s.setQtyLitres(Math.max(1, qtyLitres));
        Subscription saved = repo.save(s);
        log(customerId, "Quantity changed to " + saved.getQtyLitres() + "L");
        return saved;
    }

    // ✅ OLD pause (still supported)
    public Subscription pause(Long customerId) {
        Subscription s = getOrCreate(customerId);
        s.setStatus("PAUSED");

        if (s.getPauseStartDate() == null) {
            s.setPauseStartDate(LocalDate.now());
        }
        if (s.getPauseEndDate() == null) {
            s.setPauseEndDate(LocalDate.now()); // default same-day pause
        }

        Subscription saved = repo.save(s);
        log(customerId, "Subscription Paused");
        return saved;
    }

    // ✅ NEW pause with date range
    // Called by: PUT /pause?start=YYYY-MM-DD&end=YYYY-MM-DD
    public Subscription pause(Long customerId, String start, String end) {
        Subscription s = getOrCreate(customerId);

        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("end must be >= start");
        }

        // optional: block past dates
        LocalDate today = LocalDate.now();
        if (startDate.isBefore(today) || endDate.isBefore(today)) {
            throw new IllegalArgumentException("Only today/future dates allowed");
        }

        s.setStatus("PAUSED");
        s.setPauseStartDate(startDate);
        s.setPauseEndDate(endDate);

        Subscription saved = repo.save(s);
        log(customerId, "Subscription Paused (" + startDate + " → " + endDate + ")");
        return saved;
    }

    public Subscription resume(Long customerId) {
        Subscription s = getOrCreate(customerId);

        // ✅ PAUSE-AWARE AUTOMATION (range-aware)
        LocalDate pauseStart = s.getPauseStartDate();
        LocalDate pauseEnd = s.getPauseEndDate();

        if (pauseStart != null) {
            LocalDate effectiveEnd = (pauseEnd == null) ? LocalDate.now() : pauseEnd;

            // if resume happens before pause end, count till today only
            LocalDate countedTill = effectiveEnd.isAfter(LocalDate.now()) ? LocalDate.now() : effectiveEnd;

            long pausedDays = ChronoUnit.DAYS.between(pauseStart, countedTill) + 1; // ✅ inclusive
            if (pausedDays >= 0) {
                if (s.getEndDate() == null) s.setEndDate(LocalDate.now());
                s.setEndDate(s.getEndDate().plusDays(pausedDays));
                log(customerId, "Auto-extended by " + pausedDays + " paused day(s)");
            }

            s.setPauseStartDate(null);
            s.setPauseEndDate(null);
        }

        s.setStatus("ACTIVE");
        Subscription saved = repo.save(s);
        log(customerId, "Subscription Resumed");
        return saved;
    }

    public Subscription extend(Long customerId, int days) {
        Subscription s = getOrCreate(customerId);
        int d = Math.max(1, days);

        if (s.getEndDate() == null) s.setEndDate(LocalDate.now().plusDays(d));
        else s.setEndDate(s.getEndDate().plusDays(d));

        Subscription saved = repo.save(s);
        log(customerId, "Extended by " + d + " days");
        return saved;
    }

    /* ---------------- PATTERN-BASED PAUSE SUGGESTION ---------------- */
    public PauseSuggestionDTO getPauseSuggestion(Long customerId) {
        LocalDateTime from = LocalDateTime.now().minusDays(28);

        List<AuditLog> logs = auditRepo.findByCustomerIdOrderByTimestampDesc(customerId);

        Map<DayOfWeek, Integer> pauseCounts = new EnumMap<>(DayOfWeek.class);

        for (AuditLog a : logs) {
            if (a.getTimestamp() == null) continue;
            if (a.getTimestamp().isBefore(from)) break;

            String action = (a.getAction() == null) ? "" : a.getAction().toLowerCase();
            if (action.contains("paused")) {
                DayOfWeek d = a.getTimestamp().toLocalDate().getDayOfWeek();
                pauseCounts.put(d, pauseCounts.getOrDefault(d, 0) + 1);
            }
        }

        DayOfWeek bestDay = null;
        int bestCount = 0;
        for (var e : pauseCounts.entrySet()) {
            if (e.getValue() > bestCount) {
                bestCount = e.getValue();
                bestDay = e.getKey();
            }
        }

        PauseSuggestionDTO dto = new PauseSuggestionDTO();
        dto.setCustomerId(customerId);

        if (bestDay == null || bestCount < 2) {
            dto.setHasSuggestion(false);
            dto.setSuggestion("No strong pause pattern detected yet.");
            dto.setConfidence(0.35);
            return dto;
        }

        LocalDate next = nextOccurrence(bestDay, LocalDate.now().plusDays(1));

        dto.setHasSuggestion(true);
        dto.setSuggestedDate(next);
        dto.setReason("You paused " + bestCount + " times on " + bestDay + " in the last 28 days.");
        dto.setSuggestion("Looks like you often pause on " + bestDay + ". Want to pause on " + next + "?");
        dto.setConfidence(Math.min(0.9, 0.4 + (bestCount * 0.15)));

        return dto;
    }

    private LocalDate nextOccurrence(DayOfWeek target, LocalDate start) {
        LocalDate d = start;
        while (d.getDayOfWeek() != target) d = d.plusDays(1);
        return d;
    }
}