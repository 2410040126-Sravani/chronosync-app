package com.chronosync.dto;

import java.time.LocalDate;

public class PauseSuggestionDTO {
    private Long customerId;
    private boolean hasSuggestion;
    private String suggestion;
    private String reason;
    private LocalDate suggestedDate;
    private double confidence;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public boolean isHasSuggestion() { return hasSuggestion; }
    public void setHasSuggestion(boolean hasSuggestion) { this.hasSuggestion = hasSuggestion; }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDate getSuggestedDate() { return suggestedDate; }
    public void setSuggestedDate(LocalDate suggestedDate) { this.suggestedDate = suggestedDate; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
}