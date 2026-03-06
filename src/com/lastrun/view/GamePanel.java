package com.lastrun.view;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements ActionListener {
    private Timer timer;
    private int playerY = 250, creatureX = 0, heartsCollected = 0;
    private long startTime;

    public GamePanel() {
        timer = new Timer(20, this); // 50 FPS
        startTime = System.currentTimeMillis();
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw Runner
        g.setColor(Color.BLUE);
        g.fillRect(200, playerY, 40, 40);
        // Draw Creature
        g.setColor(Color.RED);
        g.fillRect(creatureX, 250, 50, 50);
        // Draw Stats
        g.setColor(Color.BLACK);
        g.drawString("Hearts: " + heartsCollected + "/6", 10, 20);
    }

    public void actionPerformed(ActionEvent e) {
        creatureX += 1; // Creature chases
        if (creatureX >= 150) { // Collision logic
            timer.stop();
            showPuzzle();
        }
        repaint();
    }

    private void showPuzzle() {
        // Here you call HeartAPI, show a JOptionPane with the image, 
        // and if answer is correct, heartsCollected++ and timer.restart()
    }
}