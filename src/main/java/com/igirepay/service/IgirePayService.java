package com.igirepay.service;
import com.igirepay.model.Account;
import com.igirepay.model.AccountType;
import com.igirepay.model.Customer;
import com.igirepay.model.SavingsAccount;
import com.igirepay.model.Transaction;
import com.igirepay.model.TransactionType;
import com.igirepay.model.WalletAccount;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class IgirePayService {

    private final List<Customer> customers = new ArrayList<>();
    private long nextCustomerId = 1;
    private long nextAccountId = 1;

    public IgirePayService() {
        loadSampleData();
    }

    private void loadSampleData() {
        Customer john = new Customer(nextCustomerId++, "John Doe", "john@email.com", "0788000000");

        WalletAccount wallet = new WalletAccount(nextAccountId++, john.getId(), 50000, 1234, null);
        SavingsAccount savings = new SavingsAccount(nextAccountId++, john.getId(), 100000, 1234, null);

        john.addAccount(wallet);
        john.addAccount(savings);
        customers.add(john);

        Customer mary = new Customer(nextCustomerId++, "Mary Uwase", "mary@email.com", "0788111111");
        WalletAccount maryWallet = new WalletAccount(nextAccountId++, mary.getId(), 25000, 5678, null);
        mary.addAccount(maryWallet);
        customers.add(mary);
    }

    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers);
    }

    public Optional<Account> findAccountById(long accountId) {
        for (Customer customer : customers) {
            for (Account account : customer.getAccounts()) {
                if (account.getId() == accountId) {
                    return Optional.of(account);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Customer> findCustomerByAccountId(long accountId) {
        for (Customer customer : customers) {
            for (Account account : customer.getAccounts()) {
                if (account.getId() == accountId) {
                    return Optional.of(customer);
                }
            }
        }
        return Optional.empty();
    }


    public Optional<Account> login(long accountId, int pin) {
        Optional<Account> account = findAccountById(accountId);
        if (account.isPresent() && account.get().getPin() == pin) {
            return account;
        }
        return Optional.empty();
    }

    public void deposit(long accountId, double amount, String referenceId) {
        Account account = getAccountOrThrow(accountId);
        account.deposit(amount, referenceId);
    }

    public void withdraw(long accountId, double amount, String referenceId) {
        Account account = getAccountOrThrow(accountId);
        account.withdraw(amount, referenceId);
    }


    public void transfer(long fromAccountId, long toAccountId, double amount, String referenceId) {
        if (fromAccountId == toAccountId) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        Account from = getAccountOrThrow(fromAccountId);
        Account to = getAccountOrThrow(toAccountId);

        String outRef = referenceId + "-OUT";
        String inRef = referenceId + "-IN";

        Transaction transferOut = new Transaction(0, outRef, amount, TransactionType.TRANSFER_OUT, from.getId());
        from.processTransaction(transferOut);

        Transaction transferIn = new Transaction(0, inRef, amount, TransactionType.TRANSFER_IN, to.getId());
        to.processTransaction(transferIn);
    }

    public Customer createCustomer(String fullName, String email, String phoneNumber) {
        Customer customer = new Customer(nextCustomerId++, fullName, email, phoneNumber);
        customers.add(customer);
        return customer;
    }

    public Account openAccount(long customerId, AccountType type, int pin, double startingBalance) {
        Customer customer = getCustomerOrThrow(customerId);

        Account account;
        if (type == AccountType.WALLET) {
            account = new WalletAccount(nextAccountId++, customerId, 0, pin, null);
        } else {
            account = new SavingsAccount(nextAccountId++, customerId, 0, pin, null);
        }

        if (startingBalance > 0) {
            account.deposit(startingBalance, "OPENING-BALANCE");
        }

        customer.addAccount(account);
        return account;
    }

    private Account getAccountOrThrow(long accountId) {
        return findAccountById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
    }

    private Customer getCustomerOrThrow(long customerId) {
        for (Customer customer : customers) {
            if (customer.getId() == customerId) {
                return customer;
            }
        }
        throw new IllegalArgumentException("Customer not found: " + customerId);
    }
}
