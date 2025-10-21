package com.example.test002.model;

import java.math.BigDecimal;

public class Vendor {
    private Integer vendorID;
    private String vendorName;
    private String vendorType;
    private BigDecimal price;
    private String contactNo;

    // Constructors
    public Vendor() {}

    public Vendor(String vendorName, String vendorType, BigDecimal price, String contactNo) {
        this.vendorName = vendorName;
        this.vendorType = vendorType;
        this.price = price;
        this.contactNo = contactNo;
    }

    // Getters and Setters
    public Integer getVendorID() { return vendorID; }
    public void setVendorID(Integer vendorID) { this.vendorID = vendorID; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public String getVendorType() { return vendorType; }
    public void setVendorType(String vendorType) { this.vendorType = vendorType; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    @Override
    public String toString() {
        return "Vendor{" +
                "vendorID=" + vendorID +
                ", vendorName='" + vendorName + '\'' +
                ", vendorType='" + vendorType + '\'' +
                ", price=" + price +
                ", contactNo='" + contactNo + '\'' +
                '}';
    }
}