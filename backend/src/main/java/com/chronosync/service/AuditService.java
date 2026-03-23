package com.chronosync.service;

import com.chronosync.dto.AuditEventDTO;
import com.chronosync.model.AuditLog;
import com.chronosync.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private final AuditLogRepository auditRepo;

    public AuditService(AuditLogRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    public List<AuditEventDTO> getRecent(Long customerId, int limit) {

        int safeLimit = Math.min(Math.max(limit, 1), 200);

        List<AuditLog> logs =
                auditRepo.findByCustomerIdOrderByTimestampDesc(customerId);

        return logs.stream()
                .limit(safeLimit)
                .map(a -> {
                    AuditEventDTO dto = new AuditEventDTO();

                    dto.setType(toType(a.getAction()));
                    dto.setAt(
                            a.getTimestamp() == null
                                    ? ""
                                    : a.getTimestamp().toString()
                    );
                    dto.setMeta(
                            a.getAction() == null
                                    ? ""
                                    : a.getAction()
                    );

                    return dto;
                })
                .collect(Collectors.toList());
    }

    private String toType(String action) {
        if (action == null) return "EVENT";

        String a = action.toLowerCase();

        if (a.contains("quantity")) return "QTY_CHANGE";
        if (a.contains("paused")) return "PAUSE";
        if (a.contains("resumed")) return "RESUME";
        if (a.contains("extended")) return "EXTEND";

        return "EVENT";
    }
}