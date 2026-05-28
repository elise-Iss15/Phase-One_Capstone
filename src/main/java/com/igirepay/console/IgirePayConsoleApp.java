package com.igirepay.console;

import com.igirepay.exception.DatabaseConnectionException;
import com.igirepay.exception.DuplicateTransactionException;
import com.igirepay.exception.InsufficientBalanceException;
import com.igirepay.exception.InvalidAccountException;
import com.igirepay.exception.InvalidAmountException;
import com.igirepay.model.Account;
import com.igirepay.model.Customer;
import com.igirepay.model.Transaction;
import com.igirepay.report.TransactionReportService;
import com.igirepay.service.PaymentService;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class IgirePayConsoleApp {
    private final PaymentService paymentService = new PaymentService();
    private final TransactionReportService reportService = new TransactionReportService();
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new IgirePayConsoleApp().run();
    }

    private void run() {
        printBanner();
        try {
            paymentService.testConnection();
            System.out.println("Connected to PostgreSQL.\n");
        } catch (DatabaseConnectionException e) {
            System.err.println("Database error: " + e.getMessage());
            return;
        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getMessage());
            return;
        }

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Choose option: ");
            try {
                running = handleMainChoice(choice);
            } catch (DuplicateTransactionException e) {
                System.err.println("Duplicate request: " + e.getMessage());
            } catch (InvalidAmountException | InsufficientBalanceException | InvalidAccountException e) {
                System.err.println("Transaction error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid input: " + e.getMessage());
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
            }
            System.out.println();
        }
        scanner.close();
        System.out.println("Thank you for using IgirePay.");
    }

    private boolean handleMainChoice(int choice) throws SQLException {
        return switch (choice) {
            case 1 -> { customerMenu(); yield true; }
            case 2 -> { accountMenu(); yield true; }
            case 3 -> { transactionMenu(); yield true; }
            case 4 -> { reportMenu(); yield true; }
            case 5 -> { pinMenu(); yield true; }
            case 0 -> false;
            default -> {
                System.out.println("Invalid option.");
                yield true;
            }
        };
    }

    private void customerMenu() throws SQLException {
        System.out.println("\n--- Customer Management ---");
        System.out.println("1. Register customer");
        System.out.println("2. Update customer");
        System.out.println("3. View customer accounts");
        System.out.println("0. Back");
        switch (readInt("Choice: ")) {
            case 1 -> registerCustomer();
            case 2 -> updateCustomer();
            case 3 -> viewCustomerAccounts();
            default -> { }
        }
    }

    private void accountMenu() throws SQLException {
        System.out.println("\n--- Account Management ---");
        System.out.println("1. Create wallet account");
        System.out.println("2. Create savings account");
        System.out.println("3. View balance");
        System.out.println("4. Delete inactive account");
        System.out.println("0. Back");
        switch (readInt("Choice: ")) {
            case 1 -> createAccount(true);
            case 2 -> createAccount(false);
            case 3 -> viewBalance();
            case 4 -> deleteInactive();
            default -> { }
        }
    }

    private void transactionMenu() throws SQLException {
        System.out.println("\n--- Transaction Management ---");
        System.out.println("1. Deposit");
        System.out.println("2. Withdraw");
        System.out.println("3. Transfer");
        System.out.println("4. View transaction history");
        System.out.println("0. Back");
        switch (readInt("Choice: ")) {
            case 1 -> deposit();
            case 2 -> withdraw();
            case 3 -> transfer();
            case 4 -> viewHistory();
            default -> { }
        }
    }

    private void reportMenu() throws SQLException {
        System.out.println("\n--- Reports ---");
        System.out.println("1. Export account history to CSV");
        System.out.println("2. Daily transaction summary");
        System.out.println("3. Customer statement");
        System.out.println("0. Back");
        switch (readInt("Choice: ")) {
            case 1 -> exportCsv();
            case 2 -> dailySummary();
            case 3 -> customerStatement();
            default -> { }
        }
    }

    private void pinMenu() throws SQLException {
        System.out.println("\n--- PIN Management ---");
        System.out.println("1. Set PIN (new account)");
        System.out.println("2. Validate PIN");
        System.out.println("3. Change PIN");
        System.out.println("0. Back");
        switch (readInt("Choice: ")) {
            case 1 -> setPin();
            case 2 -> validatePin();
            case 3 -> changePin();
            default -> { }
        }
    }



    private void registerCustomer() throws SQLException {
        String name = readLine("Full name: ");
        String email = readLine("Email: ");
        String phone = readLine("Phone: ");
        Customer c = paymentService.registerCustomer(name, email, phone);
        System.out.println("Registered: " + c);
    }

    private void updateCustomer() throws SQLException {
        long id = readLong("Customer ID: ");
        Customer c = paymentService.getCustomer(id);
        String name = readLine("Full name [" + c.getFullName() + "]: ");
        if (!name.isBlank()) {
            c.setFullName(name);
        }
        String email = readLine("Email [" + c.getEmail() + "]: ");
        if (!email.isBlank()) {
            c.setEmail(email);
        }
        String phone = readLine("Phone [" + c.getPhoneNumber() + "]: ");
        if (!phone.isBlank()) {
            c.setPhoneNumber(phone);
        }
        paymentService.updateCustomer(c);
        System.out.println("Customer updated.");
    }


    private void viewCustomerAccounts() throws SQLException {
        long customerId = readLong("Customer ID: ");
        List<Account> accounts = paymentService.getCustomerAccounts(customerId);
        if (accounts.isEmpty()) {
            System.out.println("No accounts for this customer.");
        } else {
            accounts.forEach(a -> System.out.println(a));
        }
    }

    private void createAccount(boolean wallet) throws SQLException {
        long customerId = readLong("Customer ID: ");
        int pin = readInt("PIN (4 digits): ");
        double opening = readDouble("Opening balance (0 if none): ");
        Account account = wallet
                ? paymentService.createWalletAccount(customerId, pin, opening)
                : paymentService.createSavingsAccount(customerId, pin, opening);
        System.out.println("Account created: " + account);
    }


    private void viewBalance() throws SQLException {
        long accountId = readLong("Account ID: ");
        System.out.printf("Balance: %.2f%n", paymentService.getBalance(accountId));
    }
    private void deleteInactive() throws SQLException {
        long accountId = readLong("Account ID: ");
        boolean deleted = paymentService.deleteInactiveAccount(accountId);
        System.out.println(deleted ? "Account deleted." : "Account not deleted (must have zero balance and no transactions).");
    }
    private void deposit() throws SQLException {
        long accountId = readLong("Account ID: ");
        double amount = readDouble("Amount: ");
        String ref = readLine("Reference ID: ");
        paymentService.deposit(accountId, amount, ref);
        System.out.println("Deposit successful.");
    }
    private void withdraw() throws SQLException {
        long accountId = readLong("Account ID: ");
        double amount = readDouble("Amount: ");
        String ref = readLine("Reference ID: ");
        paymentService.withdraw(accountId, amount, ref);
        System.out.println("Withdrawal successful.");
    }

    private void transfer() throws SQLException {
        long from = readLong("From account ID: ");
        long to = readLong("To account ID: ");
        double amount = readDouble("Amount: ");
        String ref = readLine("Reference ID: ");
        paymentService.transfer(from, to, amount, ref);
        System.out.println("Transfer successful.");
    }

    private void viewHistory() throws SQLException {
        long accountId = readLong("Account ID: ");
        List<Transaction> history = paymentService.getTransactionHistory(accountId);
        if (history.isEmpty()) {
            System.out.println("No transactions.");
        } else {
            history.forEach(System.out::println);
        }
    }

    private void exportCsv() throws SQLException {
        long accountId = readLong("Account ID: ");
        List<Transaction> history = paymentService.getTransactionHistory(accountId);
        Path path = Path.of("reports", "account-" + accountId + "-history.csv");
        try {
            java.nio.file.Files.createDirectories(path.getParent());
            reportService.exportCsv(history, path);
            System.out.println("Exported to: " + path.toAbsolutePath());
        } catch (java.io.IOException e) {
            System.err.println("Export failed: " + e.getMessage());
        }
    }

    private void dailySummary() throws SQLException {
        LocalDate date = LocalDate.now();
        Map<String, Double> summary = paymentService.getDailySummary(date);
        System.out.print(reportService.formatDailySummary(date, summary));
    }

    private void customerStatement() throws SQLException {
        long customerId = readLong("Customer ID: ");
        List<Transaction> txs = paymentService.getCustomerStatement(customerId);
        System.out.print(reportService.formatCustomerStatement(customerId, txs));
    }

    private void setPin() throws SQLException {
        long accountId = readLong("Account ID: ");
        int pin = readInt("New PIN: ");
        paymentService.setPin(accountId, pin);
        System.out.println("PIN set.");
    }

    private void validatePin() throws SQLException {
        long accountId = readLong("Account ID: ");
        int pin = readInt("PIN: ");
        boolean ok = paymentService.validatePin(accountId, pin);
        System.out.println(ok ? "PIN valid." : "PIN invalid.");
    }

    private void changePin() throws SQLException {
        long accountId = readLong("Account ID: ");
        int oldPin = readInt("Current PIN: ");
        int newPin = readInt("New PIN: ");
        paymentService.changePin(accountId, oldPin, newPin);
        System.out.println("PIN changed.");
    }

    private void printBanner() {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("      IgirePay Payment Gateway              ");
        System.out.println("╚══════════════════════════════════════════╝");
    }

    private void printMainMenu() {
        System.out.println("Main Menu");
        System.out.println("1. Customer Management");
        System.out.println("2. Account Management");
        System.out.println("3. Transaction Management");
        System.out.println("4. Reports");
        System.out.println("5. PIN Management");
        System.out.println("0. Exit");
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private long readLong(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid amount.");
            }
        }
    }
}
