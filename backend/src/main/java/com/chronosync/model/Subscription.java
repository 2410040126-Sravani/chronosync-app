package com.chronosync.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long vendorId;

    @Column(nullable = false)
    private int qtyLitres;

    @Column(nullable = false)
    private String status; // ACTIVE / PAUSED

    private LocalDate nextDeliveryDate;
    private LocalDate endDate;

    // ✅ Pause-aware automation fields
    private LocalDate pauseStartDate;

    // ✅ NEW: required for date-range pause
    private LocalDate pauseEndDate;
    
    private String customerName;
    private String customerAddress;

    /* ---------------- Getters & Setters ---------------- */

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public int getQtyLitres() { return qtyLitres; }
    public void setQtyLitres(int qtyLitres) { this.qtyLitres = qtyLitres; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getNextDeliveryDate() { return nextDeliveryDate; }
    public void setNextDeliveryDate(LocalDate nextDeliveryDate) { this.nextDeliveryDate = nextDeliveryDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getPauseStartDate() { return pauseStartDate; }
    public void setPauseStartDate(LocalDate pauseStartDate) { this.pauseStartDate = pauseStartDate; }

    public LocalDate getPauseEndDate() { return pauseEndDate; }
    public void setPauseEndDate(LocalDate pauseEndDate) { this.pauseEndDate = pauseEndDate; }
    
    public String getCustomerName() { return customerName; }
    public String getCustomerAddress() { return customerAddress; }
}