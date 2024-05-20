package com.example.semesterproject;

public class Customer {
    private String name;
    private String mobile;
    private String customerId;
    private String profilePictureUrl;

    // Default constructor required for calls to DataSnapshot.getValue(Customer.class)

    public Customer(String name, String mobile, String customerId, String profilePictureUrl) {
        this.name = name;
        this.mobile = mobile;
        this.customerId = customerId;
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
}
