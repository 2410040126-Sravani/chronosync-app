package com.chronosync.dto;

public class AuditEventDTO {

    private String type;
    private String at;
    private String meta;

    public AuditEventDTO() {
    }

    public AuditEventDTO(String type, String at, String meta) {
        this.type = type;
        this.at = at;
        this.meta = meta;
    }

    // ✅ GETTERS
    public String getType() {
        return type;
    }

    public String getAt() {
        return at;
    }

    public String getMeta() {
        return meta;
    }

    // ✅ SETTERS
    public void setType(String type) {
        this.type = type;
    }

    public void setAt(String at) {
        this.at = at;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }
}