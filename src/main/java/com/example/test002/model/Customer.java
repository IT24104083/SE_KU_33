package com.example.test002.model;

public class Customer {
    private Integer customerID;
    private String customerName;
    private String phone;
    private String email;
    private String password;

    // Constructors
    public Customer() {}

    public Customer(String customerName, String phone, String email, String password) {
        this.customerName = customerName;
        this.phone = phone;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public Integer getCustomerID() { return customerID; }
    public void setCustomerID(Integer customerID) { this.customerID = customerID; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return "Customer{" +
                "customerID=" + customerID +
                ", customerName='" + customerName + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}