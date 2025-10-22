package com.example.test002.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Inquiry {
    private Integer inquiryID;
    private BigDecimal budget;
    private LocalDate proposedDate;
    private Integer guestCount;
    private String specialRequests;
    private Integer customerID;
    private Boolean photographer;
    private Boolean catering;
    private Boolean dj;
    private Boolean decorations;
    private String eventType;
    private String status;

    // Constructors
    public Inquiry() {}

    public Inquiry(Integer customerID, BigDecimal budget, LocalDate proposedDate,
                   Integer guestCount, String eventType) {
        this.customerID = customerID;
        this.budget = budget;
        this.proposedDate = proposedDate;
        this.guestCount = guestCount;
        this.eventType = eventType;
        this.status = "Pending";
    }

    // Getters and Setters
    public Integer getInquiryID() { return inquiryID; }
    public void setInquiryID(Integer inquiryID) { this.inquiryID = inquiryID; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public LocalDate getProposedDate() { return proposedDate; }
    public void setProposedDate(LocalDate proposedDate) { this.proposedDate = proposedDate; }

    public Integer getGuestCount() { return guestCount; }
    public void setGuestCount(Integer guestCount) { this.guestCount = guestCount; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public Integer getCustomerID() { return customerID; }
    public void setCustomerID(Integer customerID) { this.customerID = customerID; }

    public Boolean getPhotographer() { return photographer; }
    public void setPhotographer(Boolean photographer) { this.photographer = photographer; }

    public Boolean getCatering() { return catering; }
    public void setCatering(Boolean catering) { this.catering = catering; }

    public Boolean getDj() { return dj; }
    public void setDj(Boolean dj) { this.dj = dj; }

    public Boolean getDecorations() { return decorations; }
    public void setDecorations(Boolean decorations) { this.decorations = decorations; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Helper methods for Thymeleaf templates
    public boolean isPhotographer() { return photographer != null && photographer; }
    public boolean isCatering() { return catering != null && catering; }
    public boolean isDj() { return dj != null && dj; }
    public boolean isDecorations() { return decorations != null && decorations; }

    @Override
    public String toString() {
        return "Inquiry{" +
                "inquiryID=" + inquiryID +
                ", budget=" + budget +
                ", proposedDate=" + proposedDate +
                ", guestCount=" + guestCount +
                ", customerID=" + customerID +
                ", eventType='" + eventType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}