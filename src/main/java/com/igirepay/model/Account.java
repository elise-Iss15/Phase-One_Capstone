package com.igirepay.model;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Account {
    private long id;
    private long customerId;
    private double balance;
    private int pin;
    private LocalDateTime createdAt;
    private final List<Transaction> transactionHistory = new ArrayList<>();


    protected Account() {
        this.balance = 0.00;
        this.createdAt = LocalDateTime.now();
    }

    protected Account(long id, long customerId, double balance, int pin, LocalDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.balance = balance;
        this.pin = pin;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public abstract AccountType getAccountType();
    public abstract void deposit(double amount, String referenceId);
    public abstract void withdraw(double amount, String referenceId);
    public abstract Transaction processTransaction(Transaction transaction);

    protected void addToHistory(Transaction transaction) {
        transactionHistory.add(transaction);
    }

    protected void applyBalanceChange(double delta) {
        this.balance += delta;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getCustomerId() { return customerId; }
    public void setCustomerId(long customerId) { this.customerId = customerId; }

    public double getBalance() { return balance; }
    protected void setBalance(double balance) { this.balance = balance; }

    public int getPin() { return pin; }
    public void setPin(int pin) { this.pin = pin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Transaction> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }

    @Override
    public String toString() {
        return "Account{id=" + id
                + ", customerId=" + customerId
                + ", type=" + getAccountType()
                + ", balance=" + balance
                + ", createdAt=" + createdAt + "}";
    }
}