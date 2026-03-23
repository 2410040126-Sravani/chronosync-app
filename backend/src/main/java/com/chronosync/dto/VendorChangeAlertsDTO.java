package com.chronosync.dto;

import com.chronosync.model.AuditLog;
import java.time.LocalDateTime;
import java.util.List;

public class VendorChangeAlertsDTO {
    private Long vendorId;
    private LocalDateTime since;

    private int qtyChanges;
    private int pauses;
    private int resumes;
    private int extendsCount;
    private int totalChanges;

    private List<AuditLog> latest;

    public VendorChangeAlertsDTO() {}

    public VendorChangeAlertsDTO(Long vendorId, LocalDateTime since) {
        this.vendorId = vendorId;
        this.since = since;
    }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public LocalDateTime getSince() { return since; }
    public void setSince(LocalDateTime since) { this.since = since; }

    public int getQtyChanges() { return qtyChanges; }
    public void setQtyChanges(int qtyChanges) { this.qtyChanges = qtyChanges; }

    public int getPauses() { return pauses; }
    public void setPauses(int pauses) { this.pauses = pauses; }

    public int getResumes() { return resumes; }
    public void setResumes(int resumes) { this.resumes = resumes; }

    public int getExtendsCount() { return extendsCount; }
    public void setExtendsCount(int extendsCount) { this.extendsCount = extendsCount; }

    public int getTotalChanges() { return totalChanges; }
    public void setTotalChanges(int totalChanges) { this.totalChanges = totalChanges; }

    public List<AuditLog> getLatest() { return latest; }
    public void setLatest(List<AuditLog> latest) { this.latest = latest; }
}