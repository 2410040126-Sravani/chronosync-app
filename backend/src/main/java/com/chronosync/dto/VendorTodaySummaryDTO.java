package com.chronosync.dto;

import java.time.LocalDate;
import java.util.List;

public class VendorTodaySummaryDTO {
    private Long vendorId;
    private LocalDate date;

    private int stops;        // ACTIVE customers count
    private int totalLitres;  // sum of qtyLitres (ACTIVE only)
    private int pausedCount;  // PAUSED subs count

    private String generatedAt; // for demo visibility

    // ✅ NEW: list for demo proof (who + where + status)
    private List<VendorTomorrowCustomerDTO> customers;

    public VendorTodaySummaryDTO() {}

    public VendorTodaySummaryDTO(Long vendorId, LocalDate date) {
        this.vendorId = vendorId;
        this.date = date;
    }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getStops() { return stops; }
    public void setStops(int stops) { this.stops = stops; }

    public int getTotalLitres() { return totalLitres; }
    public void setTotalLitres(int totalLitres) { this.totalLitres = totalLitres; }

    public int getPausedCount() { return pausedCount; }
    public void setPausedCount(int pausedCount) { this.pausedCount = pausedCount; }

    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }

    public List<VendorTomorrowCustomerDTO> getCustomers() { return customers; }
    public void setCustomers(List<VendorTomorrowCustomerDTO> customers) { this.customers = customers; }
}