package com.igirepay.db;

import com.igirepay.exception.DatabaseConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public final class DatabaseConnection {

    private static final String PROPERTIES_FILE = "application.properties";
    private static String url;
    private static String user;
    private static String password;

    static {
        loadProperties();
    }

    private DatabaseConnection() {
    }

    private static void loadProperties() {
        Properties props = new Properties();
        try (InputStream in = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {
            if (in == null) {
                throw new DatabaseConnectionException(
                        "Missing " + PROPERTIES_FILE + " on classpath", null);
            }
            props.load(in);
            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");
        } catch (IOException e) {
            throw new DatabaseConnectionException("Failed to load database config", e);
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                    "Could not connect to PostgreSQL. Check application.properties and that the server is running.",
                    e);
        }
    }
}
