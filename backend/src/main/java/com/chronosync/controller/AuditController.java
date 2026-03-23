package com.chronosync.controller;

import com.chronosync.model.AuditLog;
import com.chronosync.repository.AuditLogRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "http://localhost:3000")
public class AuditController {

    private final AuditLogRepository repo;

    public AuditController(AuditLogRepository repo) {
        this.repo = repo;
    }

    // GET http://localhost:8082/api/audit/1
    @GetMapping("/{customerId}")
    public List<AuditLog> timeline(@PathVariable Long customerId) {
        return repo.findByCustomerIdOrderByTimestampDesc(customerId);
    }
}
