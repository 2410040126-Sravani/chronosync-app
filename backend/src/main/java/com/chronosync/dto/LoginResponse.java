package com.chronosync.dto;

public class LoginResponse {

    private String token;
    private String role;
    private Long userId;
    private String name;
    private Long subscriptionId; // 🔥 NEW FIELD

    public LoginResponse() {
    }

    // 🔥 UPDATED CONSTRUCTOR
    public LoginResponse(String token, String role, Long userId, String name, Long subscriptionId) {
        this.token = token;
        this.role = role;
        this.userId = userId;
        this.name = name;
        this.subscriptionId = subscriptionId;
    }

    // ===== GETTERS & SETTERS =====

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // 🔥 NEW GETTER & SETTER

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
}