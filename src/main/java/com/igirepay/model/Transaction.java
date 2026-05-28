package com.igirepay.model;
import java.time.LocalDateTime;

public class Transaction {
    private long transactionId;
    private String referenceId;
    private double amount;
    private TransactionType transactionType;
    private LocalDateTime timestamp;
    private long accountId;


    public Transaction() {
        this.timestamp = LocalDateTime.now();
    }

    public Transaction(long transactionId, String referenceId, double amount,
                       TransactionType transactionType, long accountId) {
        this.transactionId = transactionId;
        this.referenceId = referenceId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.accountId = accountId;
        this.timestamp = LocalDateTime.now();
    }

    public long getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }
    public String getReferenceId() {
        return referenceId;
    }
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) { this.amount = amount; }

    public TransactionType getTransactionType() {
        return transactionType;
    }
    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public long getAccountId() {
        return accountId;
    }
    public void setAccountId(long accountId) { this.accountId = accountId; }

    @Override
    public String toString() {
        return "Transaction{id=" + transactionId
                + ", referenceId='" + referenceId + '\''
                + ", amount=" + amount
                + ", type=" + transactionType
                + ", accountId=" + accountId
                + ", timestamp=" + timestamp + "}";
    }

    public int getType() {
        return 0;
    }
}