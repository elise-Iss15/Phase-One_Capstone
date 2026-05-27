package com.igirepay.dao;

import com.igirepay.db.DatabaseConnection;
import com.igirepay.model.Transaction;
import com.igirepay.model.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionDAO {

    public Transaction create(Transaction transaction) throws SQLException {
        String sql = """
                INSERT INTO transactions (account_id, reference_id, transaction_type, amount)
                VALUES (?, ?, ?, ?) RETURNING id, created_at
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, transaction.getAccountId());
            ps.setString(2, transaction.getReferenceId());
            ps.setString(3, transaction.getTransactionType().name());
            ps.setDouble(4, transaction.getAmount());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    transaction.setTransactionId(rs.getLong("id"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        transaction.setTimestamp(ts.toLocalDateTime());
                    }
                }
            }
        }
        return transaction;
    }

    public List<Transaction> findByAccountId(long accountId) throws SQLException {
        String sql = """
                SELECT id, account_id, reference_id, transaction_type, amount, created_at
                FROM transactions WHERE account_id = ? ORDER BY created_at DESC
                """;
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Transaction> findByCustomerId(long customerId) throws SQLException {
        String sql = """
                SELECT t.id, t.account_id, t.reference_id, t.transaction_type, t.amount, t.created_at
                FROM transactions t
                JOIN accounts a ON a.id = t.account_id
                WHERE a.customer_id = ?
                ORDER BY t.created_at DESC
                """;
        List<Transaction> list = new ArrayList<>();
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

    public Map<String, Double> dailySummary(LocalDate date) throws SQLException {
        String sql = """
                SELECT transaction_type, SUM(amount) AS total
                FROM transactions
                WHERE DATE(created_at) = ?
                GROUP BY transaction_type
                """;
        Map<String, Double> summary = new HashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    summary.put(rs.getString("transaction_type"), rs.getDouble("total"));
                }
            }
        }
        return summary;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction tx = new Transaction();
        tx.setTransactionId(rs.getLong("id"));
        tx.setAccountId(rs.getLong("account_id"));
        tx.setReferenceId(rs.getString("reference_id"));
        tx.setTransactionType(TransactionType.valueOf(rs.getString("transaction_type")));
        tx.setAmount(rs.getDouble("amount"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            tx.setTimestamp(ts.toLocalDateTime());
        }
        return tx;
    }
}
