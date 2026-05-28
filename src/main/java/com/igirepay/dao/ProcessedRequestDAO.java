package com.igirepay.dao;

import com.igirepay.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class ProcessedRequestDAO {
    public boolean exists(String referenceId) throws SQLException {
        String sql = "SELECT 1 FROM processed_requests WHERE reference_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, referenceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void markProcessed(String referenceId) throws SQLException {
        String sql = "INSERT INTO processed_requests (reference_id) VALUES (?) ON CONFLICT (reference_id) DO NOTHING";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, referenceId);
            ps.executeUpdate();
        }
    }

    public boolean markProcessedIfNew(String referenceId) throws SQLException {
        String sql = "INSERT INTO processed_requests (reference_id) VALUES (?) ON CONFLICT DO NOTHING RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, referenceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
