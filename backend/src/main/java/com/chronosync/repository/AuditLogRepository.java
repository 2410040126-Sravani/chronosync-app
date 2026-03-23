package com.chronosync.repository;

import com.chronosync.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByCustomerIdOrderByTimestampDesc(Long customerId);

    List<AuditLog> findByCustomerIdInAndTimestampAfterOrderByTimestampDesc(
            List<Long> customerIds,
            LocalDateTime since
    );

    // ✅ NEW: all-time logs for a vendor's customers (no time filter)
    List<AuditLog> findByCustomerIdInOrderByTimestampDesc(List<Long> customerIds);
}