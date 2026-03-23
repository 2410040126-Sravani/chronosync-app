package com.chronosync.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_sync_state")
public class VendorSyncState {

    @Id
    private Long vendorId;   // primary key = vendor

    // ✅ MUST be nullable, so UI can show "Not yet"
    @Column(nullable = true)
    private LocalDateTime lastSyncedAt;

    public VendorSyncState() {}

    public VendorSyncState(Long vendorId, LocalDateTime lastSyncedAt) {
        this.vendorId = vendorId;
        this.lastSyncedAt = lastSyncedAt;
    }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(LocalDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
}