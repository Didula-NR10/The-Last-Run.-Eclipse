package com.lastrun.view;
import javax.swing.*;

import com.lastrun.model.DatabaseManager;

import java.awt.*;

public class Dashboard extends JFrame {
    private String username;
    private CardLayout cardLayout = new CardLayout();
    private JPanel contentPanel = new JPanel(cardLayout);
    private DatabaseManager db = new DatabaseManager();

    public Dashboard(String username) {
        this.username = username;
        setTitle("The Last Run - Dashboard");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. Create Navigation Bar (The Side/Top Menu)
        JPanel navBar = new JPanel(new GridLayout(1, 4, 10, 0));
        navBar.setBackground(new Color(44, 62, 80)); // Dark professional color

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

        // 3. Button Events (Event-Driven Programming)
        btnStart.addActionListener(e -> {
            this.dispose(); 
            new GameWindow(username); // Launch the runner
        });
        
        btnScores.addActionListener(e -> cardLayout.show(contentPanel, "scores"));
        btnProfile.addActionListener(e -> cardLayout.show(contentPanel, "profile"));
        
        btnLogout.addActionListener(e -> {
            this.dispose();
            new AuthWindow();
        });

        // Add components to JFrame
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
        panel.add(new JLabel("Welcome back, " + username + "! Ready for the Last Run?"));
        return panel;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 200, 50, 200));

        JLabel nameLabel = new JLabel("User Name: " + username);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        // Fetch current bio from DB
        String currentBio = db.getUserBio(username); 
        JTextArea bioArea = new JTextArea(currentBio);
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
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Global Leaderboard (Shortest Time)", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(label, BorderLayout.NORTH);

        // You could use a JTable here later to show all top scores from DB
        return panel;
    }
}