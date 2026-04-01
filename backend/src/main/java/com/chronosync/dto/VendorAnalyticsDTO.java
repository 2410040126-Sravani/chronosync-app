
package com.chronosync.dto;

import java.time.LocalDateTime;

public class VendorAnalyticsDTO {

    private Long vendorId;
    private LocalDateTime since;

    // ✅ Real metrics
    private double deliveredMilkL;
    private double milkSavedL;

    // optional metadata
    private LocalDateTime computedAt;

    // 🔥 ADD THIS (NEW FIELD)
    private String insight;

    public VendorAnalyticsDTO() {}

    public VendorAnalyticsDTO(Long vendorId, LocalDateTime since) {
        this.vendorId = vendorId;
        this.since = since;
    }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public LocalDateTime getSince() { return since; }
    public void setSince(LocalDateTime since) { this.since = since; }

    public double getDeliveredMilkL() { return deliveredMilkL; }
    public void setDeliveredMilkL(double deliveredMilkL) { this.deliveredMilkL = deliveredMilkL; }

    public double getMilkSavedL() { return milkSavedL; }
    public void setMilkSavedL(double milkSavedL) { this.milkSavedL = milkSavedL; }

    public LocalDateTime getComputedAt() { return computedAt; }
    public void setComputedAt(LocalDateTime computedAt) { this.computedAt = computedAt; }

    // ✅ NEW GETTER
    public String getInsight() {
        return insight;
    }

    // ✅ NEW SETTER
    public void setInsight(String insight) {
        this.insight = insight;
    }
}

