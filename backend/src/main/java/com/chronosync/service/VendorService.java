
package com.chronosync.service;

import com.chronosync.dto.VendorTomorrowCustomerDTO;
import com.chronosync.dto.VendorAnalyticsDTO;
import com.chronosync.dto.VendorTodaySummaryDTO;
import com.chronosync.dto.VendorChangeAlertsDTO;
import com.chronosync.model.Subscription;
import com.chronosync.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import com.chronosync.repository.AuditLogRepository;
import com.chronosync.model.AuditLog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VendorService {

    private final SubscriptionRepository subRepo;
    private final AuditLogRepository auditRepo;

    public VendorService(SubscriptionRepository subRepo, AuditLogRepository auditRepo) {
        this.subRepo = subRepo;
        this.auditRepo = auditRepo;
    }

    // ===============================
    // 🔥 SMART INSIGHT (NEW - REAL APP)
    // ===============================
    public String getSmartInsight(Long vendorId) {

        List<Subscription> subs = subRepo.findByVendorId(vendorId);

        int paused = 0;
        int total = subs.size();
        int totalQty = 0;

        for (Subscription s : subs) {
            totalQty += s.getQtyLitres();
            if ("PAUSED".equalsIgnoreCase(s.getStatus())) paused++;
        }

        double pauseRate = total == 0 ? 0 : (paused * 100.0 / total);

        if (pauseRate > 40) {
            return "🚨 High pause rate! Customers skipping deliveries frequently";
        }

        if (totalQty > 20) {
            return "🔥 High demand today — prepare extra supply";
        }

        if (paused == 0) {
            return "✅ Perfect day — no interruptions";
        }

        return "ℹ️ Normal activity";
    }
    // ===============================
    // ✅ PAUSE SUGGESTION
    // ===============================
    public String getPauseSuggestion(Long vendorId) {

        List<Subscription> subs = subRepo.findByVendorId(vendorId);

        List<Long> customerIds = subs.stream()
                .map(Subscription::getCustomerId)
                .toList();

        List<AuditLog> logs =
                auditRepo.findByCustomerIdInOrderByTimestampDesc(customerIds);

        Map<String, Integer> dayCount = new HashMap<>();

        for (AuditLog log : logs) {
            if ("PAUSE".equals(log.getAction())) {
                String day = log.getTimestamp().getDayOfWeek().toString();
                dayCount.put(day, dayCount.getOrDefault(day, 0) + 1);
            }
        }

        String bestDay = null;
        int max = 0;

        for (var e : dayCount.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                bestDay = e.getKey();
            }
        }

        if (bestDay == null) return "💡 No pause pattern found";

        return "💡 Customers often pause on " + bestDay;
    }

    // ===============================
    // ✅ TOMORROW PREVIEW
    // ===============================
    public VendorTodaySummaryDTO getTomorrowPreview(Long vendorId) {

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        List<Subscription> subs = subRepo.findByVendorId(vendorId);

        int stops = 0;
        int totalLitres = 0;
        int pausedCount = 0;

        List<VendorTomorrowCustomerDTO> customerList = new ArrayList<>();

        for (Subscription s : subs) {

            boolean isPaused = false;
            String pauseStart = null;
            String pauseEnd = null;

            if (s.getPauses() != null) {
                for (var p : s.getPauses()) {
                    if (!tomorrow.isBefore(p.getStartDate()) &&
                        !tomorrow.isAfter(p.getEndDate())) {

                        isPaused = true;
                        pauseStart = p.getStartDate().toString();
                        pauseEnd = p.getEndDate().toString();
                        break;
                    }
                }
            }

            if (!isPaused) {
                totalLitres += s.getQtyLitres();
                stops++;
            } else {
                pausedCount++;
            }

            VendorTomorrowCustomerDTO dto =
                    new VendorTomorrowCustomerDTO(
                            s.getCustomerId(),
                            s.getCustomerName(),
                            s.getCustomerAddress(),
                            s.getQtyLitres(),
                            isPaused ? "PAUSED" : "ACTIVE"
                    );

            dto.setPauseStart(pauseStart);
            dto.setPauseEnd(pauseEnd);

            customerList.add(dto);
        }

        VendorTodaySummaryDTO dto = new VendorTodaySummaryDTO();
        dto.setVendorId(vendorId);
        dto.setDate(tomorrow);
        dto.setStops(stops);
        dto.setTotalLitres(totalLitres);
        dto.setPausedCount(pausedCount);
        dto.setGeneratedAt(LocalDateTime.now().toString());
        dto.setCustomers(customerList);

        return dto;
    }

    // ===============================
    // ✅ TODAY CUSTOMERS
    // ===============================
    public List<Subscription> getTodayCustomers(Long vendorId) {

        LocalDate today = LocalDate.now();

        List<Subscription> subs = subRepo.findByVendorId(vendorId);

        for (Subscription s : subs) {

            boolean isPaused = false;

            if (s.getPauses() != null) {
                for (var p : s.getPauses()) {
                    if (!today.isBefore(p.getStartDate()) &&
                        !today.isAfter(p.getEndDate())) {

                        isPaused = true;
                        break;
                    }
                }
            }

            s.setStatus(isPaused ? "PAUSED" : "ACTIVE");
        }

        return subs;
    }

    // ===============================
    // 🔥 ANALYTICS (UPGRADED)
    // ===============================
    public VendorAnalyticsDTO getAnalyticsPreferSnapshot(Long vendorId, String window) {

        List<Subscription> subs = subRepo.findByVendorId(vendorId);

        int totalMilk = 0;
        int pausedCount = 0;

        for (Subscription s : subs) {

            boolean isPaused = false;

            if (s.getPauses() != null) {
                for (var p : s.getPauses()) {
                    if (!LocalDate.now().isBefore(p.getStartDate()) &&
                        !LocalDate.now().isAfter(p.getEndDate())) {

                        isPaused = true;
                        break;
                    }
                }
            }

            if (isPaused) {
                pausedCount++;
            } else {
                totalMilk += s.getQtyLitres();
            }
        }

        VendorAnalyticsDTO dto = new VendorAnalyticsDTO();
        dto.setVendorId(vendorId);
        dto.setDeliveredMilkL(totalMilk);
        dto.setMilkSavedL(pausedCount * 2);
        dto.setComputedAt(LocalDateTime.now());
        
        // 🔥 REAL SMART INSIGHT
        dto.setInsight(getSmartInsight(vendorId));

        return dto;
    }

    // ===============================
    // ✅ CHANGE ALERTS
    // ===============================
    public VendorChangeAlertsDTO getChangeAlerts(Long vendorId) {

        List<Subscription> subs = subRepo.findByVendorId(vendorId);

        List<Long> customerIds = new ArrayList<>();
        for (Subscription s : subs) {
            customerIds.add(s.getCustomerId());
        }

        List<AuditLog> logs =
                auditRepo.findByCustomerIdInOrderByTimestampDesc(customerIds);

        VendorChangeAlertsDTO dto = new VendorChangeAlertsDTO();

        int qty = 0, pause = 0, resume = 0;
        List<AuditLog> latest = new ArrayList<>();

        for (AuditLog log : logs) {

            String type = log.getAction();

            if ("QTY_CHANGE".equals(type)) qty++;
            if ("PAUSE".equals(type)) pause++;
            if ("RESUME".equals(type)) resume++;

            if (latest.size() < 5) {
                latest.add(log);
            }
        }

        dto.setTotalChanges(logs.size());
        dto.setQtyChanges(qty);
        dto.setPauses(pause);
        dto.setResumes(resume);
        dto.setLatest(latest);

        return dto;
    }

    // ===============================
    // ✅ MARK SYNC
    // ===============================
    public com.chronosync.model.VendorSyncState markSynced(Long vendorId) {
        return new com.chronosync.model.VendorSyncState();
    }
}

