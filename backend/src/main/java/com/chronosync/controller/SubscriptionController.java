package com.chronosync.controller;

import com.chronosync.dto.AuditEventDTO;
import com.chronosync.model.Subscription;
import com.chronosync.service.SubscriptionService;
import com.chronosync.service.AuditService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@CrossOrigin(origins = "*")
public class SubscriptionController {

    private final SubscriptionService service;
    private final AuditService auditService;

    // ===============================
    // CONSTRUCTOR
    // ===============================
    public SubscriptionController(SubscriptionService subscriptionService,
                                  AuditService auditService) {
        this.service = subscriptionService;
        this.auditService = auditService;
    }

    // ===============================
    // GET SUBSCRIPTION
    // ===============================
    @GetMapping("/{customerId}")
    public Subscription get(@PathVariable Long customerId) {
        return service.get(customerId);
    }

    // ===============================
    // UPDATE QUANTITY
    // ===============================
    @PutMapping("/{customerId}/qty")
    public Subscription qty(@PathVariable Long customerId,
                            @RequestParam int value) {
        return service.updateQty(customerId, value);
    }

    // ===============================
    // PAUSE
    // ===============================
    @PutMapping("/{customerId}/pause")
    public Subscription pause(
            @PathVariable Long customerId,
            @RequestParam String start,
            @RequestParam String end
    ) {
        return service.pause(customerId, start, end);
    }

    // ===============================
    // RESUME
    // ===============================
    @PutMapping("/{customerId}/resume")
    public Subscription resume(@PathVariable Long customerId) {
        return service.resume(customerId);
    }

    // ===============================
    // EXTEND
    // ===============================
    @PutMapping("/{customerId}/extend")
    public Subscription extend(@PathVariable Long customerId,
                               @RequestParam int days) {
        return service.extend(customerId, days);
    }

    // ===============================
    // AUDIT
    // ===============================
    @GetMapping("/{customerId}/audit")
    public List<AuditEventDTO> audit(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "30") int limit
    ) {
        return auditService.getRecent(customerId, limit);
    }
    @GetMapping("/vendor/{vendorId}/pause-suggestion")
    public String pauseSuggestion(@PathVariable Long vendorId) {
        return service.getPauseSuggestion(vendorId);
    }
    // ===============================
    // 🔥 GET ALL CUSTOMERS FOR VENDOR
    // ===============================
    @GetMapping("/vendor/{vendorId}")
    public List<Subscription> getByVendor(@PathVariable Long vendorId) {
        return service.getByVendorId(vendorId);
    }
}