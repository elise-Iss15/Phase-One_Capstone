package com.igirepay.model;
import com.igirepay.exception.InsufficientBalanceException;
import com.igirepay.exception.InvalidAmountException;
import java.time.LocalDateTime;

public class WalletAccount extends Account {

    public WalletAccount() {
        super();
    }

    public WalletAccount(long id, long customerId, double balance, int pin, LocalDateTime createdAt) {
        super(id, customerId, balance, pin, createdAt);
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.WALLET;
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
        if (getBalance() < amount) {
            throw new InsufficientBalanceException(
                    "Insufficient wallet balance. Have: " + getBalance() + ", need: " + amount);
        }
        applyBalanceChange(-amount);
        Transaction tx = new Transaction(0, referenceId, amount, TransactionType.WITHDRAWAL, getId());
        addToHistory(tx);
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
                        "Unsupported transaction type for wallet: " + transaction.getTransactionType()
                );
        }

        return transaction;
    }

    private void validateAmount(double amount) {
        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }
    }

    @Override
    public String toString() {
        return "WalletAccount{" + super.toString() + "}";
    }
}