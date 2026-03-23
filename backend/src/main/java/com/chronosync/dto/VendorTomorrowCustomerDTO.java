package com.chronosync.dto;

public class VendorTomorrowCustomerDTO {
    private Long customerId;
    private String customerName;
    private String customerAddress;
    private int qtyLitres;
    private String status;

    public VendorTomorrowCustomerDTO() {}

    public VendorTomorrowCustomerDTO(Long customerId, String customerName, String customerAddress,
                                     int qtyLitres, String status) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.qtyLitres = qtyLitres;
        this.status = status;
    }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public int getQtyLitres() { return qtyLitres; }
    public void setQtyLitres(int qtyLitres) { this.qtyLitres = qtyLitres; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}