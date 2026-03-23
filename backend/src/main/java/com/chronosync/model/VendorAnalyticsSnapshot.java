package com.chronosync.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_analytics_snapshot")
public class VendorAnalyticsSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Long vendorId;

    // Example: "monthToDate"
    @Column(nullable=false)
    private String windowKey;

    // For MTD snapshots, we store which date this snapshot is for
    @Column(nullable=false)
    private LocalDate snapshotDate;

    @Column(nullable=false)
    private double deliveredMilkL;

    @Column(nullable=false)
    private double milkSavedL;

    @Column(nullable=false)
    private LocalDateTime computedAt;

    public VendorAnalyticsSnapshot() {}

    public VendorAnalyticsSnapshot(Long vendorId, String windowKey, LocalDate snapshotDate,
                                   double deliveredMilkL, double milkSavedL, LocalDateTime computedAt) {
        this.vendorId = vendorId;
        this.windowKey = windowKey;
        this.snapshotDate = snapshotDate;
        this.deliveredMilkL = deliveredMilkL;
        this.milkSavedL = milkSavedL;
        this.computedAt = computedAt;
    }

    public Long getId() { return id; }
    public Long getVendorId() { return vendorId; }
    public String getWindowKey() { return windowKey; }
    public LocalDate getSnapshotDate() { return snapshotDate; }
    public double getDeliveredMilkL() { return deliveredMilkL; }
    public double getMilkSavedL() { return milkSavedL; }
    public LocalDateTime getComputedAt() { return computedAt; }

    public void setId(Long id) { this.id = id; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public void setWindowKey(String windowKey) { this.windowKey = windowKey; }
    public void setSnapshotDate(LocalDate snapshotDate) { this.snapshotDate = snapshotDate; }
    public void setDeliveredMilkL(double deliveredMilkL) { this.deliveredMilkL = deliveredMilkL; }
    public void setMilkSavedL(double milkSavedL) { this.milkSavedL = milkSavedL; }
    public void setComputedAt(LocalDateTime computedAt) { this.computedAt = computedAt; }
}