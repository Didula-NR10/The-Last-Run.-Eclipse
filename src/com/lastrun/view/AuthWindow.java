package com.lastrun.view;

import javax.swing.*;

import com.lastrun.model.DatabaseManager;

import java.awt.*;
import java.awt.event.*;

public class AuthWindow extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);
    private DatabaseManager db = new DatabaseManager(); // Your MySQL Manager

    public AuthWindow() {
        setTitle("The Last Run - Login & Register");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Add panels to the card layout
        mainPanel.add(createLoginPanel(), "login");
        mainPanel.add(createRegisterPanel(), "register");

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField userField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);
        JButton loginBtn = new JButton("Login");
        JButton switchRegBtn = new JButton("Create New Account");

        // UI Layout
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; panel.add(userField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; panel.add(passField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; panel.add(loginBtn, gbc);
        gbc.gridy = 3; panel.add(switchRegBtn, gbc);

        // Event: Switch to Register
        switchRegBtn.addActionListener(e -> cardLayout.show(mainPanel, "register"));

        // Event: Login Logic (Virtual Identity)
        loginBtn.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            if (db.loginUser(username, password)) {
                JOptionPane.showMessageDialog(this, "Welcome, " + username + "!");
                this.dispose(); 
                new Dashboard(username); // Now opens the professional Dashboard
            
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField userField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);
        JButton regBtn = new JButton("Register");
        JButton backBtn = new JButton("Back to Login");

        // UI Layout
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("New Username:"), gbc);
        gbc.gridx = 1; panel.add(userField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1; panel.add(passField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; panel.add(regBtn, gbc);
        gbc.gridy = 3; panel.add(backBtn, gbc);

        // Event: Switch back
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        // Event: Register Logic
        regBtn.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
            } else if (db.registerUser(username, password)) {
                JOptionPane.showMessageDialog(this, "Registration successful! Please login.");
                cardLayout.show(mainPanel, "login");
            } else {
                JOptionPane.showMessageDialog(this, "Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AuthWindow());
    }
}