package com.chronosync.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "vendor_today_summary")
public class VendorTodaySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long vendorId;

    private LocalDate summaryDate;

    private int stops;
    private int totalLitres;
    private int pausedCount;

    private String generatedAt;

    public VendorTodaySummary() {}

    public VendorTodaySummary(Long vendorId, LocalDate summaryDate) {
        this.vendorId = vendorId;
        this.summaryDate = summaryDate;
    }

    public Long getId() { return id; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public LocalDate getSummaryDate() { return summaryDate; }
    public void setSummaryDate(LocalDate summaryDate) { this.summaryDate = summaryDate; }

    public int getStops() { return stops; }
    public void setStops(int stops) { this.stops = stops; }

    public int getTotalLitres() { return totalLitres; }
    public void setTotalLitres(int totalLitres) { this.totalLitres = totalLitres; }

    public int getPausedCount() { return pausedCount; }
    public void setPausedCount(int pausedCount) { this.pausedCount = pausedCount; }

    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
}