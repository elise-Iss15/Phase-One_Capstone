package com.igirepay.service;

import com.igirepay.exception.DuplicateTransactionException;
import com.igirepay.model.Account;
import com.igirepay.model.Customer;
import com.igirepay.model.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class PaymentManager {

    private final List<Customer> customers = new ArrayList<>();
    private final List<Transaction> transactionHistory = new ArrayList<>();
    private final Set<String> processedReferenceIds = new HashSet<>();
    private final Map<String, String> failedTransactionLogs = new HashMap<>();

    public List<Customer> getCustomers() {
        return new ArrayList<>(customers);
    }

    public void registerCustomer(Customer customer) {
        customers.add(customer);
    }

    public List<Transaction> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }

    public Set<String> getProcessedReferenceIds() {
        return new HashSet<>(processedReferenceIds);
    }

    public Map<String, String> getFailedTransactionLogs() {
        return new HashMap<>(failedTransactionLogs);
    }


    public void ensureReferenceNotProcessed(String referenceId) {
        if (!processedReferenceIds.add(referenceId)) {
            throw new DuplicateTransactionException(
                    "Duplicate transaction request rejected. Reference already processed: " + referenceId);
        }
    }

    public void recordTransaction(Transaction transaction) {
        transactionHistory.add(transaction);
    }

    public void logFailure(String referenceId, String reason) {
        failedTransactionLogs.put(referenceId, reason);
        processedReferenceIds.remove(referenceId);
    }

    public void addAccountToCustomer(long customerId, Account account) {
        for (Customer customer : customers) {
            if (customer.getId() == customerId) {
                customer.addAccount(account);
                return;
            }
        }
        throw new IllegalArgumentException("Customer not found: " + customerId);
    }
}
