package com.chronosync.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===============================
    // BASIC FIELDS
    // ===============================
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_address")
    private String customerAddress;

    @Column(name = "qty_litres")
    private int qtyLitres;

    @Column(name = "next_delivery_date")
    private LocalDate nextDeliveryDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // ===============================
    // ✅ NEW FIELD (VERY IMPORTANT)
    // ===============================
    @Transient
    private LocalDate effectiveEndDate;

    // ===============================
    // OTHER FIELDS
    // ===============================
    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "status")
    private String status; // ACTIVE / PAUSED

    // ===============================
    // RELATIONSHIP
    // ===============================
    @OneToMany(
        mappedBy = "subscription",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    private List<PausePeriod> pauses;

    /* ---------------- Getters & Setters ---------------- */

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public int getQtyLitres() { return qtyLitres; }
    public void setQtyLitres(int qtyLitres) { this.qtyLitres = qtyLitres; }

    public LocalDate getNextDeliveryDate() { return nextDeliveryDate; }
    public void setNextDeliveryDate(LocalDate nextDeliveryDate) { this.nextDeliveryDate = nextDeliveryDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    // ✅ EFFECTIVE END DATE (NEW)
    public LocalDate getEffectiveEndDate() {
        return effectiveEndDate;
    }

    public void setEffectiveEndDate(LocalDate effectiveEndDate) {
        this.effectiveEndDate = effectiveEndDate;
    }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<PausePeriod> getPauses() { return pauses; }
    public void setPauses(List<PausePeriod> pauses) { this.pauses = pauses; }
}