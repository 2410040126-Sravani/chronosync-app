package com.chronosync.service;

import com.chronosync.dto.VendorAnalyticsDTO;
import com.chronosync.dto.VendorChangeAlertsDTO;
import com.chronosync.dto.VendorTodaySummaryDTO;
import com.chronosync.dto.VendorTomorrowCustomerDTO;
import com.chronosync.model.AuditLog;
import com.chronosync.model.Subscription;
import com.chronosync.model.VendorSyncState;
import com.chronosync.repository.AuditLogRepository;
import com.chronosync.repository.SubscriptionRepository;
import com.chronosync.repository.VendorAnalyticsSnapshotRepository;
import com.chronosync.repository.VendorSyncStateRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VendorService {

    private final SubscriptionRepository subRepo;
    private final AuditLogRepository auditRepo;
    private final VendorSyncStateRepository syncRepo;
    private final VendorAnalyticsSnapshotRepository snapRepo;

    public VendorService(SubscriptionRepository subRepo,
                         AuditLogRepository auditRepo,
                         VendorSyncStateRepository syncRepo,
                         VendorAnalyticsSnapshotRepository snapRepo) {
        this.subRepo = subRepo;
        this.auditRepo = auditRepo;
        this.syncRepo = syncRepo;
        this.snapRepo = snapRepo;
    }

    /* ----------------- HELPERS ----------------- */

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private boolean contains(String text, String... keys) {
        if (text == null) return false;
        String t = text.toLowerCase();
        for (String k : keys) {
            if (t.contains(k.toLowerCase())) return true;
        }
        return false;
    }

    private double getSubQtyLitres(Subscription s) {
        return (s == null) ? 0.0 : s.getQtyLitres();
    }

    private List<Long> getVendorCustomerIds(Long vendorId) {
        List<Subscription> subs = subRepo.findByVendorId(vendorId);
        return subs.stream()
                .map(Subscription::getCustomerId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /* ----------------- SYNC ----------------- */

    public VendorSyncState markSynced(Long vendorId) {
        VendorSyncState state = syncRepo.findById(vendorId)
                .orElseGet(() -> new VendorSyncState(vendorId, null));
        state.setLastSyncedAt(LocalDateTime.now());
        return syncRepo.save(state);
    }

    /* ----------------- CHANGE ALERTS ----------------- */

    public VendorChangeAlertsDTO getChangeAlerts(Long vendorId) {

        LocalDateTime since = syncRepo.findById(vendorId)
                .map(VendorSyncState::getLastSyncedAt)
                .orElse(LocalDateTime.now().minusHours(24));

        List<Long> customerIds = getVendorCustomerIds(vendorId);
        VendorChangeAlertsDTO dto = new VendorChangeAlertsDTO(vendorId, since);

        if (customerIds.isEmpty()) {
            dto.setTotalChanges(0);
            dto.setQtyChanges(0);
            dto.setPauses(0);
            dto.setResumes(0);
            dto.setExtendsCount(0);
            dto.setLatest(List.of());
            return dto;
        }

        List<AuditLog> logs =
                auditRepo.findByCustomerIdInAndTimestampAfterOrderByTimestampDesc(customerIds, since);

        dto.setTotalChanges(logs.size());
        dto.setQtyChanges((int) logs.stream().filter(x -> contains(x.getAction(), "qty", "quantity")).count());
        dto.setPauses((int) logs.stream().filter(x -> contains(x.getAction(), "pause")).count());
        dto.setResumes((int) logs.stream().filter(x -> contains(x.getAction(), "resume")).count());
        dto.setExtendsCount((int) logs.stream().filter(x -> contains(x.getAction(), "extend")).count());

        dto.setLatest(logs.stream().limit(8).toList());
        return dto;
    }

    /* ----------------- ANALYTICS ----------------- */

    public VendorAnalyticsDTO getAnalytics(Long vendorId, String window) {

        LocalDateTime now = LocalDateTime.now();
        String w = (window == null || window.isBlank()) ? "monthToDate" : window;

        LocalDateTime since;
        if ("24h".equalsIgnoreCase(w)) {
            since = now.minusHours(24);
        } else if ("sinceSync".equalsIgnoreCase(w)) {
            since = syncRepo.findById(vendorId)
                    .map(VendorSyncState::getLastSyncedAt)
                    .orElse(now.minusHours(24));
        } else {
            since = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        }

        VendorAnalyticsDTO dto = new VendorAnalyticsDTO(vendorId, since);
        dto.setComputedAt(now);

        List<Long> customerIds = getVendorCustomerIds(vendorId);
        if (customerIds.isEmpty()) {
            dto.setDeliveredMilkL(0);
            dto.setMilkSavedL(0);
            return dto;
        }

        List<Subscription> subs = subRepo.findByVendorId(vendorId);

        long days = Duration.between(since, now).toDays() + 1;

        double totalQtyPerDay = subs.stream()
                .filter(s -> s.getStatus() != null && s.getStatus().equalsIgnoreCase("ACTIVE"))
                .mapToDouble(this::getSubQtyLitres)
                .sum();

        double plannedMilk = totalQtyPerDay * days;

        List<AuditLog> logs =
                auditRepo.findByCustomerIdInAndTimestampAfterOrderByTimestampDesc(customerIds, since);

        Map<Long, Integer> qtyByCustomer = subs.stream()
                .filter(s -> s.getCustomerId() != null)
                .collect(Collectors.toMap(
                        Subscription::getCustomerId,
                        Subscription::getQtyLitres,
                        (a, b) -> a
                ));

        double saved = 0.0;
        for (AuditLog log : logs) {
            if (log.getAction() != null && log.getAction().equalsIgnoreCase("Subscription Paused")) {
                int qty = qtyByCustomer.getOrDefault(log.getCustomerId(), 0);
                saved += qty;
            }
        }

        double delivered = Math.max(0, plannedMilk - saved);

        dto.setMilkSavedL(round1(saved));
        dto.setDeliveredMilkL(round1(delivered));
        return dto;
    }

    /* ----------------- SNAPSHOT PREFERRED ----------------- */

    public VendorAnalyticsDTO getAnalyticsPreferSnapshot(Long vendorId, String window) {

        String w = (window == null || window.isBlank()) ? "monthToDate" : window;

        if ("monthToDate".equalsIgnoreCase(w)) {
            LocalDate today = LocalDate.now();

            return snapRepo
                    .findTopByVendorIdAndWindowKeyAndSnapshotDateOrderByComputedAtDesc(
                            vendorId, "monthToDate", today
                    )
                    .map(s -> {
                        VendorAnalyticsDTO dto = new VendorAnalyticsDTO(vendorId, null);
                        dto.setComputedAt(s.getComputedAt());
                        dto.setDeliveredMilkL(s.getDeliveredMilkL());
                        dto.setMilkSavedL(s.getMilkSavedL());
                        return dto;
                    })
                    .orElseGet(() -> getAnalytics(vendorId, w));
        }

        return getAnalytics(vendorId, w);
    }

    /* ----------------- TOMORROW PREVIEW (BEST DEMO) ----------------- */

    public VendorTodaySummaryDTO getTomorrowPreview(Long vendorId) {

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<Subscription> subs = subRepo.findByVendorId(vendorId);

        int stops = 0;
        int totalLitres = 0;
        int pausedCount = 0;

        List<VendorTomorrowCustomerDTO> customerList = new ArrayList<>();

        for (Subscription s : subs) {

            if (s.getStatus() == null) continue;

            // count totals
            if (s.getStatus().equalsIgnoreCase("ACTIVE")) {
                totalLitres += s.getQtyLitres();
                stops++;
            }

            if (s.getStatus().equalsIgnoreCase("PAUSED")) {
                pausedCount++;
            }

            // build customer DTO
            VendorTomorrowCustomerDTO customerDTO =
                    new VendorTomorrowCustomerDTO(
                            s.getCustomerId(),
                            s.getCustomerName(),
                            s.getCustomerAddress(),
                            s.getQtyLitres(),
                            s.getStatus()
                    );

            customerList.add(customerDTO);
        }

        VendorTodaySummaryDTO dto = new VendorTodaySummaryDTO();
        dto.setVendorId(vendorId);
        dto.setDate(tomorrow);
        dto.setStops(stops);
        dto.setTotalLitres(totalLitres);
        dto.setPausedCount(pausedCount);
        dto.setGeneratedAt(LocalDateTime.now().toString());

        // ✅ IMPORTANT
        dto.setCustomers(customerList);

        return dto;
    }
}