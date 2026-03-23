package com.chronosync.controller;

import com.chronosync.dto.PauseSuggestionDTO;
import com.chronosync.dto.AuditEventDTO;
import com.chronosync.model.Subscription;
import com.chronosync.service.SubscriptionService;
import com.chronosync.service.AuditService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@CrossOrigin(origins = "http://localhost:5173") // ✅ Vite port (change if needed)
public class SubscriptionController {

    private final SubscriptionService service;
    private final AuditService auditService;

    public SubscriptionController(SubscriptionService service, AuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping("/{customerId}")
    public Subscription get(@PathVariable Long customerId) {
        return service.getSubscription(customerId);
    }

    @PutMapping("/{customerId}/qty")
    public Subscription qty(@PathVariable Long customerId, @RequestParam int value) {
        return service.updateQty(customerId, value);
    }

    // ✅ Pause accepts date range (query params)
    // Example:
    // PUT http://localhost:8082/api/subscriptions/1/pause?start=2026-02-27&end=2026-03-01
    @PutMapping("/{customerId}/pause")
    public Subscription pause(
            @PathVariable Long customerId,
            @RequestParam String start,
            @RequestParam String end
    ) {
        return service.pause(customerId, start, end);
    }

    @PutMapping("/{customerId}/resume")
    public Subscription resume(@PathVariable Long customerId) {
        return service.resume(customerId);
    }

    @PutMapping("/{customerId}/extend")
    public Subscription extend(@PathVariable Long customerId, @RequestParam int days) {
        return service.extend(customerId, days);
    }

    @GetMapping("/{customerId}/pause-suggestion")
    public PauseSuggestionDTO pauseSuggestion(@PathVariable Long customerId) {
        return service.getPauseSuggestion(customerId);
    }

    // ✅ NEW: Activity/Audit Timeline Endpoint (unique + demo-friendly)
    // Example: GET http://localhost:8082/api/subscriptions/1/audit?limit=30
    @GetMapping("/{customerId}/audit")
    public List<AuditEventDTO> audit(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "30") int limit
    ) {
        return auditService.getRecent(customerId, limit);
    }
}