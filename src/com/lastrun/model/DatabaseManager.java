package com.lastrun.model;
import java.sql.*;

public class DatabaseManager {
    // Database credentials for Localhost MySQL
    private final String URL = "jdbc:mysql://localhost:3306/lastrun_db";
    private final String DB_USER = "root"; // Default XAMPP user
    private final String DB_PASS = "";     // Default XAMPP password is empty

    public Connection getConnection() throws SQLException {
        // This ensures the driver is loaded for Java 1.8
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL, DB_USER, DB_PASS);
    }

    // REGISTER USER
    public boolean registerUser(String user, String pass) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user);
            pstmt.setString(2, pass);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Registration error: " + e.getMessage());
            return false;
        }
    }

    // LOGIN USER
    public boolean loginUser(String user, String pass) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user);
            pstmt.setString(2, pass);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }
 // Fetches the Bio for the profile page
    public String getUserBio(String username) {
        String bio = "";
        String sql = "SELECT bio FROM users WHERE username = ?";
        try (Connection conn = getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                bio = rs.getString("bio");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Return empty string if bio is null to avoid errors in UI
        return (bio == null) ? "" : bio;
    }
    // UPDATE PROFILE (BIO)
    public boolean updateBio(String username, String newBio) {
        String sql = "UPDATE users SET bio = ? WHERE username = ?";
        try (Connection conn = getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newBio);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}