package com.chronosync.controller;

import com.chronosync.dto.VendorAnalyticsDTO;
import com.chronosync.dto.VendorChangeAlertsDTO;
import com.chronosync.dto.VendorTodaySummaryDTO;
import com.chronosync.model.VendorSyncState;
import com.chronosync.service.VendorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendor")
@CrossOrigin(origins = "http://localhost:5173")
public class VendorController {

    private final VendorService service;

    public VendorController(VendorService service) {
        this.service = service;
    }

    @GetMapping("/{vendorId}/change-alerts")
    public VendorChangeAlertsDTO changeAlerts(@PathVariable Long vendorId) {
        return service.getChangeAlerts(vendorId);
    }
    @GetMapping("/{vendorId}/pause-suggestion") public String pauseSuggestion(@PathVariable Long vendorId) { return service.getPauseSuggestion(vendorId); }

    @PostMapping("/{vendorId}/mark-synced")
    public VendorSyncState markSynced(@PathVariable Long vendorId) {
        return service.markSynced(vendorId);
    }

    @GetMapping("/{vendorId}/today-summary")
    public VendorTodaySummaryDTO todaySummary(@PathVariable Long vendorId) {
        return service.getTodaySummary(vendorId);
    }
    
    @GetMapping("/{vendorId}/analytics")
    public VendorAnalyticsDTO analytics(@PathVariable Long vendorId,
                                        @RequestParam(required = false) String window) {
        return service.getAnalyticsPreferSnapshot(vendorId, window);
    }

    // ✅ NEW: Tomorrow Preview (with customer list)
    @GetMapping("/{vendorId}/tomorrow-preview")
    public VendorTodaySummaryDTO tomorrowPreview(@PathVariable Long vendorId) {
        return service.getTomorrowPreview(vendorId);
    }
    
    @GetMapping("/{vendorId}/customers")
    public java.util.List<com.chronosync.model.Subscription> customers(@PathVariable Long vendorId) {
        return service.getTodayCustomers(vendorId);
    }
}