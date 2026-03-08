package com.lastrun.model;

import java.sql.*;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

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

    // --- AUTHENTICATION METHODS ---

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

    // --- PROFILE / BIO METHODS ---

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
        return (bio == null) ? "" : bio;
    }

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

    // --- SCOREBOARD / LEADERBOARD METHODS ---

    /**
     * Saves game results quietly to the database.
     */
    public boolean saveScore(String user, int stones, int seconds) {
        String sql = "INSERT INTO scores (username, stones_collected, time_seconds) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user);
            pstmt.setInt(2, stones);
            pstmt.setInt(3, seconds);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Score Save Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper for Dashboard to get a TableModel for the Highest Scores tab.
     * Sorts by most stones first, then lowest time.
     */
    public DefaultTableModel getScoresTableModel() {
        Vector<String> columns = new Vector<>();
        columns.add("Rank");
        columns.add("Survivor Name");
        columns.add("Stones Collected");
        columns.add("Survival Time");

        Vector<Vector<Object>> data = new Vector<>();
        String sql = "SELECT username, stones_collected, time_seconds FROM scores " +
                     "ORDER BY stones_collected DESC, time_seconds ASC LIMIT 10";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int rank = 1;
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add("#" + rank++);
                row.add(rs.getString("username"));
                row.add(rs.getInt("stones_collected"));
                
                // Format time as MM:SS for better UI
                int totalSecs = rs.getInt("time_seconds");
                row.add(String.format("%02d:%02d", totalSecs / 60, totalSecs % 60));
                
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Leaderboard Data Fetch Error: " + e.getMessage());
        }
        return new DefaultTableModel(data, columns);
    }
}