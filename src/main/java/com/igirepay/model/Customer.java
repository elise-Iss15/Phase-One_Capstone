package com.igirepay.model;

import java.util.ArrayList;
import java.util.List;

public class Customer {

    private long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private final List<Account> accounts = new ArrayList<>();

    public Customer() {}

    public Customer(long id, String fullName, String email, String phoneNumber) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public List<Account> getAccounts() {
        return new ArrayList<>(accounts);
    }

    @Override
    public String toString() {
        return "Customer{id=" + id
                + ", fullName='" + fullName + '\''
                + ", email='" + email + '\''
                + ", phoneNumber='" + phoneNumber + '\''
                + ", accountCount=" + accounts.size() + "}";
    }
}