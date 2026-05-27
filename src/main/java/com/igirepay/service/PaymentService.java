package com.igirepay.service;

import com.igirepay.dao.AccountDAO;
import com.igirepay.dao.CustomerDAO;
import com.igirepay.dao.ProcessedRequestDAO;
import com.igirepay.dao.TransactionDAO;
import com.igirepay.db.DatabaseConnection;
import com.igirepay.exception.DuplicateTransactionException;
import com.igirepay.exception.InvalidAccountException;
import com.igirepay.model.Account;
import com.igirepay.model.AccountType;
import com.igirepay.model.Customer;
import com.igirepay.model.SavingsAccount;
import com.igirepay.model.Transaction;
import com.igirepay.model.TransactionType;
import com.igirepay.model.WalletAccount;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


public class PaymentService {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final ProcessedRequestDAO processedRequestDAO = new ProcessedRequestDAO();
    private final PaymentManager paymentManager = new PaymentManager();

    public PaymentManager getPaymentManager() {
        return paymentManager;
    }

    public void testConnection() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (!conn.isValid(3)) {
                throw new SQLException("Database connection is not valid");
            }
        }
    }

    public Customer registerCustomer(String fullName, String email, String phone) throws SQLException {
        Customer customer = new Customer(0, fullName, email, phone);
        return customerDAO.create(customer);
    }

    public boolean updateCustomer(Customer customer) throws SQLException {
        return customerDAO.update(customer);
    }

    public Customer getCustomer(long id) throws SQLException {
        return customerDAO.findById(id)
                .orElseThrow(() -> new InvalidAccountException("Customer not found: " + id));
    }

    public List<Customer> listCustomers() throws SQLException {
        return customerDAO.findAll();
    }

    public List<Account> getCustomerAccounts(long customerId) throws SQLException {
        return accountDAO.findByCustomerId(customerId);
    }

    public Account createWalletAccount(long customerId, int pin, double openingBalance) throws SQLException {
        return createAccount(customerId, AccountType.WALLET, pin, openingBalance);
    }

    public Account createSavingsAccount(long customerId, int pin, double openingBalance) throws SQLException {
        return createAccount(customerId, AccountType.SAVINGS, pin, openingBalance);
    }

    private Account createAccount(long customerId, AccountType type, int pin, double openingBalance)
            throws SQLException {
        getCustomer(customerId);
        Account account = type == AccountType.WALLET ? new WalletAccount() : new SavingsAccount();
        account.setCustomerId(customerId);
        account.setPin(pin);
        accountDAO.create(account);
        if (openingBalance > 0) {
            deposit(account.getId(), openingBalance, "OPEN-" + account.getId());
        }
        return accountDAO.findById(account.getId()).orElse(account);
    }

    public double getBalance(long accountId) throws SQLException {
        return getAccount(accountId).getBalance();
    }

    public boolean deleteInactiveAccount(long accountId) throws SQLException {
        return accountDAO.deleteInactive(accountId);
    }

    public boolean validatePin(long accountId, int pin) throws SQLException {
        return accountDAO.validatePin(accountId, pin);
    }

    public void changePin(long accountId, int oldPin, int newPin) throws SQLException {
        if (!accountDAO.validatePin(accountId, oldPin)) {
            throw new InvalidAccountException("Invalid PIN for account " + accountId);
        }
        accountDAO.updatePin(accountId, newPin);
    }

    public void setPin(long accountId, int pin) throws SQLException {
        accountDAO.updatePin(accountId, pin);
    }

    public void deposit(long accountId, double amount, String referenceId) throws SQLException {
        runIdempotent(referenceId, () -> {
            Account account = getAccount(accountId);
            account.deposit(amount, referenceId);
            persistAccountState(account);
        });
    }

    public void withdraw(long accountId, double amount, String referenceId) throws SQLException {
        runIdempotent(referenceId, () -> {
            Account account = getAccount(accountId);
            account.withdraw(amount, referenceId);
            persistAccountState(account);
        });
    }


    public void transfer(long fromId, long toId, double amount, String referenceId) throws SQLException {
        if (fromId == toId) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        if (processedRequestDAO.exists(referenceId)) {
            throw new DuplicateTransactionException("Duplicate transfer reference: " + referenceId);
        }
        paymentManager.ensureReferenceNotProcessed(referenceId);

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Account from = getAccount(fromId);
                Account to = getAccount(toId);

                String outRef = referenceId + "-OUT";
                String inRef = referenceId + "-IN";

                from.processTransaction(
                        new Transaction(0, outRef, amount, TransactionType.TRANSFER_OUT, fromId));
                to.processTransaction(
                        new Transaction(0, inRef, amount, TransactionType.TRANSFER_IN, toId));

                accountDAO.updateBalance(fromId, from.getBalance());
                accountDAO.updateBalance(toId, to.getBalance());
                persistNewTransactions(from);
                persistNewTransactions(to);

                processedRequestDAO.markProcessed(referenceId);
                paymentManager.recordTransaction(
                        new Transaction(0, referenceId, amount, TransactionType.TRANSFER_OUT, fromId));
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                paymentManager.logFailure(referenceId, e.getMessage());
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Transaction> getTransactionHistory(long accountId) throws SQLException {
        return transactionDAO.findByAccountId(accountId);
    }

    public List<Transaction> getCustomerStatement(long customerId) throws SQLException {
        return transactionDAO.findByCustomerId(customerId);
    }

    public Map<String, Double> getDailySummary(LocalDate date) throws SQLException {
        return transactionDAO.dailySummary(date);
    }

    private void runIdempotent(String referenceId, SqlRunnable action) throws SQLException {
        if (processedRequestDAO.exists(referenceId)) {
            throw new DuplicateTransactionException(
                    "Duplicate transaction request rejected. Reference: " + referenceId);
        }
        paymentManager.ensureReferenceNotProcessed(referenceId);
        try {
            action.run();
            processedRequestDAO.markProcessed(referenceId);
        } catch (DuplicateTransactionException e) {
            paymentManager.logFailure(referenceId, e.getMessage());
            throw e;
        } catch (RuntimeException | SQLException e) {
            paymentManager.logFailure(referenceId, e.getMessage());
            throw e;
        }
    }

    private void persistAccountState(Account account) throws SQLException {
        accountDAO.updateBalance(account.getId(), account.getBalance());
        persistNewTransactions(account);
    }

    private void persistNewTransactions(Account account) throws SQLException {
        for (Transaction tx : account.getTransactionHistory()) {
            if (tx.getTransactionId() == 0) {
                transactionDAO.create(tx);
                paymentManager.recordTransaction(tx);
            }
        }
    }

    private Account getAccount(long accountId) throws SQLException {
        return accountDAO.findById(accountId)
                .orElseThrow(() -> new InvalidAccountException("Account not found: " + accountId));
    }

    @FunctionalInterface
    private interface SqlRunnable {
        void run() throws SQLException;
    }
}
