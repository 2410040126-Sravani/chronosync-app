
package com.chronosync.controller;

import com.chronosync.dto.AuditEventDTO;
import com.chronosync.service.AuditService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    // Existing (customer timeline)
    @GetMapping("/{customerId}")
    public List<AuditEventDTO> timeline(@PathVariable Long customerId) {
        return auditService.getRecent(customerId, 50);
    }

    // ✅ NEW: Vendor notifications
    @PostMapping("/vendor")
    public List<AuditEventDTO> vendorNotifications(@RequestBody List<Long> customerIds) {
        return auditService.getVendorNotifications(customerIds, 20);
    }
}