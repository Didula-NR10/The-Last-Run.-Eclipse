package com.lastrun.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.lastrun.model.DatabaseManager;
import java.awt.*;

public class Dashboard extends JFrame {
    private String username;
    private CardLayout cardLayout = new CardLayout();
    private JPanel contentPanel = new JPanel(cardLayout);
    private DatabaseManager db = new DatabaseManager();
    
    // We define the table here so we can update its data later
    private JTable scoreTable = new JTable();

    public Dashboard(String username) {
        this.username = username;
        setTitle("The Last Run - Dashboard");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. Create Navigation Bar
        JPanel navBar = new JPanel(new GridLayout(1, 4, 10, 0));
        navBar.setBackground(new Color(44, 62, 80));

        JButton btnStart = createNavButton("Start Game");
        JButton btnScores = createNavButton("Highest Scores");
        JButton btnProfile = createNavButton("User Profile");
        JButton btnLogout = createNavButton("Logout");

        navBar.add(btnStart);
        navBar.add(btnScores);
        navBar.add(btnProfile);
        navBar.add(btnLogout);

        // 2. Create the different Views (Cards)
        contentPanel.add(createHomePanel(), "home");
        contentPanel.add(createScorePanel(), "scores");
        contentPanel.add(createProfilePanel(), "profile");

        // 3. Button Events
        btnStart.addActionListener(e -> {
            this.dispose(); 
            new GameWindow(username); 
        });
        
        btnScores.addActionListener(e -> {
            // REFRESH DATA: Every time the button is clicked, fetch new scores
            scoreTable.setModel(db.getScoresTableModel());
            cardLayout.show(contentPanel, "scores");
        });

        btnProfile.addActionListener(e -> cardLayout.show(contentPanel, "profile"));
        
        btnLogout.addActionListener(e -> {
            this.dispose();
            new AuthWindow();
        });

        add(navBar, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(52, 73, 94));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return btn;
    }

    // --- VIEW PANELS ---

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel welcomeLabel = new JLabel("Welcome back, " + username + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(welcomeLabel);
        return panel;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 200, 50, 200));

        JLabel nameLabel = new JLabel("User Name: " + username);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        String currentBio = db.getUserBio(username); 
        JTextArea bioArea = new JTextArea(currentBio);
        bioArea.setLineWrap(true);
        JButton saveBtn = new JButton("Update Bio");

        panel.add(nameLabel);
        panel.add(new JLabel("Your Bio:"));
        panel.add(new JScrollPane(bioArea));
        panel.add(saveBtn);

        saveBtn.addActionListener(e -> {
            db.updateBio(username, bioArea.getText());
            JOptionPane.showMessageDialog(this, "Profile Updated!");
        });

        return panel;
    }

    private JPanel createScorePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("🏆 Global Leaderboard (Top 10 Survivors)", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Styling the table for a professional look
        scoreTable.setRowHeight(30);
        scoreTable.setFont(new Font("Arial", Font.PLAIN, 14));
        scoreTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        scoreTable.setFillsViewportHeight(true);
        scoreTable.setEnabled(false); // Make it read-only

        JScrollPane scrollPane = new JScrollPane(scoreTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
}