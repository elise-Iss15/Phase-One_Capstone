package com.igirepay.model;

import com.igirepay.exception.InsufficientBalanceException;
import com.igirepay.exception.InvalidAmountException;
import java.time.LocalDateTime;

public class SavingsAccount extends Account {

    private static final double WITHDRAWAL_FEE_RATE = 0.01;
    private static final double MAX_SINGLE_WITHDRAWAL = 500000;
    private int withdrawalsThisMonth;
    public SavingsAccount() {
        super();
    }
    public SavingsAccount(long id, long customerId, double balance, int pin, LocalDateTime createdAt) {
        super(id, customerId, balance, pin, createdAt);
    }


    @Override
    public AccountType getAccountType() {
        return AccountType.SAVINGS;
    }

    @Override
    public void deposit(double amount, String referenceId) {
        validateAmount(amount);
        applyBalanceChange(amount);
        Transaction tx = new Transaction(0, referenceId, amount, TransactionType.DEPOSIT, getId());
        addToHistory(tx);
    }

    @Override
    public void withdraw(double amount, String referenceId) {
        validateAmount(amount);
        if (amount > MAX_SINGLE_WITHDRAWAL) {
            throw new InvalidAmountException(
                    "Savings withdrawal exceeds limit of " + MAX_SINGLE_WITHDRAWAL);
        }
        if (withdrawalsThisMonth >= 3) {
            throw new InvalidAmountException(
                    "Savings account allows only 3 withdrawals per month");
        }
        double fee = amount * WITHDRAWAL_FEE_RATE;
        double totalDebit = amount + fee;
        if (getBalance() < totalDebit) {
            throw new InsufficientBalanceException(
                    "Insufficient savings balance. Need " + totalDebit + ", have " + getBalance());
        }
        applyBalanceChange(-totalDebit);
        withdrawalsThisMonth++;
        addToHistory(new Transaction(0, referenceId, amount, TransactionType.WITHDRAWAL, getId()));
        if (fee > 0) {
            addToHistory(new Transaction(0, referenceId + "-FEE", fee, TransactionType.FEE, getId()));
        }
    }

    @Override
    public Transaction processTransaction(Transaction transaction) {
        validateAmount(transaction.getAmount());
        switch (transaction.getTransactionType()) {
            case DEPOSIT:
            case TRANSFER_IN:
                deposit(transaction.getAmount(), transaction.getReferenceId());
                break;
            case WITHDRAWAL:
            case TRANSFER_OUT:
                withdraw(transaction.getAmount(), transaction.getReferenceId());
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported transaction type: " + transaction.getTransactionType());
        }
        return transaction;
    }

    private void validateAmount(double amount) {
        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }
    }

    public int getWithdrawalsThisMonth() {
        return withdrawalsThisMonth;
    }

    @Override
    public String toString() {
        return "SavingsAccount{" + super.toString()
                + ", withdrawalsThisMonth=" + withdrawalsThisMonth + "}";
    }
}
