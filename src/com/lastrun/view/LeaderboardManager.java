package com.lastrun.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.Dimension;
import java.sql.*;
import java.util.Vector;

public class LeaderboardManager {
    // Database credentials - Update these to match your setup!
    private static final String URL = "jdbc:mysql://localhost:3306/your_db_name";
    private static final String USER = "root";
    private static final String PASS = "your_password";

    public static void saveAndShow(JFrame parent, String name, int stones, int seconds) {
        saveScore(name, stones, seconds);
        displayTop10(parent);
    }

    private static void saveScore(String name, int stones, int seconds) {
        String query = "INSERT INTO scores (username, stones_collected, time_seconds) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, name);
            pstmt.setInt(2, stones);
            pstmt.setInt(3, seconds);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error saving score: " + e.getMessage());
        }
    }

    private static void displayTop10(JFrame parent) {
        String query = "SELECT username, stones_collected, time_seconds FROM scores " +
                       "ORDER BY stones_collected DESC, time_seconds ASC LIMIT 10";

        // Table Columns
        Vector<String> columns = new Vector<>();
        columns.add("Rank");
        columns.add("Player");
        columns.add("Stones");
        columns.add("Time");

        Vector<Vector<Object>> data = new Vector<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            int rank = 1;
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rank++);
                row.add(rs.getString("username"));
                row.add(rs.getInt("stones_collected"));
                
                // Format seconds back to MM:SS for the table
                int s = rs.getInt("time_seconds");
                row.add(String.format("%02d:%02d", s / 60, s % 60));
                
                data.add(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parent, "Leaderboard unavailable.");
        }

        JTable table = new JTable(new DefaultTableModel(data, columns));
        table.setEnabled(false); // Make it read-only
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(400, 250));
        
        JOptionPane.showMessageDialog(parent, scrollPane, "🏆 TOP 10 LEGENDS", JOptionPane.PLAIN_MESSAGE);
    }
}