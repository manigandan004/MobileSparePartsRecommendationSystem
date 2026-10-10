
package com.msp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {

    private Database() {
        // Utility class; do not instantiate.
    }

    private static final String URL =
        System.getenv().getOrDefault(
            "MSP_DB_URL",
            "jdbc:mysql://localhost:3306/mobispares?serverTimezone=UTC"
        );

    private static final String USER =
        System.getenv().getOrDefault("MSP_DB_USER", "root");

    private static final String PASSWORD =
        System.getenv().getOrDefault("MSP_DB_PASSWORD", "");

    /**
     * Creates and returns a new MySQL connection.
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "MySQL JDBC driver is missing. Check pom.xml.",
                e
            );
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Tests the database connection and prints its status.
     */
    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            System.out.println(
                "MySQL database connected successfully! Database: "
                + connection.getCatalog()
            );
            return true;
        } catch (SQLException e) {
            System.err.println("MySQL database connection failed!");
            System.err.println("Reason: " + e.getMessage());
            return false;
        }
    }
}
