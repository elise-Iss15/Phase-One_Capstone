package com.igirepay.dao;

import com.igirepay.db.DatabaseConnection;
import com.igirepay.model.Account;
import com.igirepay.model.AccountType;
import com.igirepay.model.SavingsAccount;
import com.igirepay.model.WalletAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountDAO {
    public Account create(Account account) throws SQLException {
        String sql = """
                INSERT INTO accounts (customer_id, account_type, balance, pin)
                VALUES (?, ?, ?, ?) RETURNING id, created_at
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, account.getCustomerId());
            ps.setString(2, account.getAccountType().name());
            ps.setDouble(3, account.getBalance());
            ps.setInt(4, account.getPin());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    account.setId(rs.getLong("id"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        account.setCreatedAt(ts.toLocalDateTime());
                    }
                }
            }
        }
        return account;
    }

    public Optional<Account> findById(long id) throws SQLException {
        String sql = "SELECT id, customer_id, account_type, balance, pin, created_at FROM accounts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Account> findByCustomerId(long customerId) throws SQLException {
        String sql = """
                SELECT id, customer_id, account_type, balance, pin, created_at
                FROM accounts WHERE customer_id = ? ORDER BY id
                """;
        List<Account> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public boolean updateBalance(long accountId, double balance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, balance);
            ps.setLong(2, accountId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePin(long accountId, int newPin) throws SQLException {
        String sql = "UPDATE accounts SET pin = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newPin);
            ps.setLong(2, accountId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteInactive(long accountId) throws SQLException {
        String sql = """
                DELETE FROM accounts
                WHERE id = ? AND balance = 0
                AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.account_id = accounts.id)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean validatePin(long accountId, int pin) throws SQLException {
        String sql = "SELECT 1 FROM accounts WHERE id = ? AND pin = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            ps.setInt(2, pin);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        long customerId = rs.getLong("customer_id");
        AccountType type = AccountType.valueOf(rs.getString("account_type"));
        double balance = rs.getDouble("balance");
        int pin = rs.getInt("pin");
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

        if (type == AccountType.WALLET) {
            return new WalletAccount(id, customerId, balance, pin, createdAt);
        }
        return new SavingsAccount(id, customerId, balance, pin, createdAt);


    }
}
