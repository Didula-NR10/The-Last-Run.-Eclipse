package com.lastrun.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.io.*;
import java.net.HttpURLConnection;
import javax.imageio.ImageIO;
import com.lastrun.model.DatabaseManager; // Import your DB manager

/**
 * The Last Run: Top-Down Survival Edition
 */
public class GameWindow extends JFrame implements ActionListener, KeyListener {
    // 1. Database and User Info
    private String username;
    private DatabaseManager db = new DatabaseManager(); 
    
    // 2. Timers
    private Timer gameTimer;   // For movement and physics (20ms)
    private Timer clockTimer;  // For the stopwatch (1000ms)
    
    // 3. Player and Monster Settings
    private int playerX = 400, playerY = 300, pSize = 30;
    private int monsterX = 50, monsterY = 50, mSize = 40;
    private int stoneX, stoneY;
    private Color currentStoneColor;
    
    // 4. Stats
    private int stonesCollected = 0;
    private int secondsElapsed = 0;
    
    // 5. Game Objects
    private List<Rectangle> obstacles = new ArrayList<>();
    private boolean[] keys = new boolean[256];
    
    private final Color[] colors = {
        Color.RED, Color.GREEN, Color.BLUE, 
        Color.ORANGE, Color.YELLOW, new Color(128, 0, 128) // Purple
    };
    
    public GameWindow(String username) {
        this.username = username;
        setTitle("The Last Run: Survival Mode - " + username);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        setupLevel();
        spawnStone();
        
        GamePanel panel = new GamePanel();
        add(panel);
        addKeyListener(this);
        setFocusable(true);
        
        // Physics and Movement Timer
        gameTimer = new Timer(20, this);
        gameTimer.start();

        // Stopwatch Timer
        clockTimer = new Timer(1000, e -> secondsElapsed++);
        clockTimer.start();
        
        setVisible(true);
    }

    private void setupLevel() {
        obstacles.clear();
        obstacles.add(new Rectangle(0, 0, 800, 10));   
        obstacles.add(new Rectangle(0, 550, 800, 10)); 
        obstacles.add(new Rectangle(0, 0, 10, 600));   
        obstacles.add(new Rectangle(775, 0, 10, 600)); 
        
        obstacles.add(new Rectangle(200, 150, 150, 30));
        obstacles.add(new Rectangle(500, 100, 30, 200));
        obstacles.add(new Rectangle(150, 400, 30, 100));
        obstacles.add(new Rectangle(400, 400, 200, 30));
    }

    private void spawnStone() {
        do {
            stoneX = (int) (Math.random() * 700) + 50;
            stoneY = (int) (Math.random() * 450) + 50;
        } while (isColliding(stoneX, stoneY, 25, 25));

        int chance = (int)(Math.random() * 100); 
        if (chance < 40) {
            currentStoneColor = Color.RED; 
        } else {
            currentStoneColor = colors[(int)(Math.random() * (colors.length - 1)) + 1];
        }
    }

    private boolean isColliding(int x, int y, int w, int h) {
        Rectangle nextPos = new Rectangle(x, y, w, h);
        for (Rectangle r : obstacles) {
            if (nextPos.intersects(r)) return true;
        }
        return false;
    }

    class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(new Color(25, 25, 35));
            g.fillRect(0, 0, 800, 600);

            g.setColor(new Color(80, 80, 80));
            for (Rectangle r : obstacles) g.fillRect(r.x, r.y, r.width, r.height);

            g.setColor(currentStoneColor);
            g.fillOval(stoneX, stoneY, 25, 25);

            g.setColor(Color.CYAN);
            g.fillRect(playerX, playerY, pSize, pSize);

            g.setColor(Color.RED);
            g.fillRect(monsterX, monsterY, mSize, mSize);

            // UI: Timer and Counter
            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.BOLD, 16));
            
            String timeStr = String.format("%02d:%02d", secondsElapsed / 60, secondsElapsed % 60);
            g.drawString("Survivor: " + username, 20, 30);
            g.drawString("Time: " + timeStr, 20, 55);
            g.drawString("Stones: " + stonesCollected, 20, 80);
            
            if (currentStoneColor == Color.RED) {
                g.setColor(Color.RED);
                g.drawString("!! RED STONE: API PUZZLE !!", 20, 105);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int nextX = playerX, nextY = playerY;
        int speed = 5;

        if (keys[KeyEvent.VK_UP]) nextY -= speed;
        if (keys[KeyEvent.VK_DOWN]) nextY += speed;
        if (keys[KeyEvent.VK_LEFT]) nextX -= speed;
        if (keys[KeyEvent.VK_RIGHT]) nextX += speed;

        if (!isColliding(nextX, playerY, pSize, pSize)) playerX = nextX;
        if (!isColliding(playerX, nextY, pSize, pSize)) playerY = nextY;

        // Monster AI
        int mNextX = monsterX, mNextY = monsterY;
        if (monsterX < playerX) mNextX += 2; else if (monsterX > playerX) mNextX -= 2;
        if (monsterY < playerY) mNextY += 2; else if (monsterY > playerY) mNextY -= 2;

        if (!isColliding(mNextX, monsterY, mSize, mSize)) monsterX = mNextX;
        if (!isColliding(monsterX, mNextY, mSize, mSize)) monsterY = mNextY;

        // Item Collection
        Rectangle pRect = new Rectangle(playerX, playerY, pSize, pSize);
        if (pRect.intersects(new Rectangle(stoneX, stoneY, 25, 25))) {
            if (currentStoneColor == Color.RED) {
                gameTimer.stop();
                clockTimer.stop(); 
                new Thread(this::triggerHeartPuzzle).start();
            } else {
                stonesCollected++;
                spawnStone();
            }
        }

        // Caught Logic
        if (pRect.intersects(new Rectangle(monsterX, monsterY, mSize, mSize))) {
            handleGameOver("The creature caught you after " + secondsElapsed + " seconds!");
        }
        repaint();
    }

    private void triggerHeartPuzzle() {
        try {
            URL url = new URL("https://marcconrad.com/uob/heart/api.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String json = in.readLine();
            in.close();

            if (json != null) {
                String imgUrl = json.split("\"question\"\\s*:\\s*\"")[1].split("\"")[0];
                String correctAns = json.split("\"solution\"\\s*:\\s*")[1].split(",")[0].trim();

                Image image = ImageIO.read(new URL(imgUrl));
                ImageIcon icon = new ImageIcon(image);

                SwingUtilities.invokeLater(() -> {
                    Object input = JOptionPane.showInputDialog(this, 
                        "HEART CHALLENGE: Solve to continue!", "API Puzzle", 
                        JOptionPane.PLAIN_MESSAGE, icon, null, "");

                    if (input != null && input.toString().equals(correctAns)) {
                        JOptionPane.showMessageDialog(this, "Correct!");
                        stonesCollected++;
                        spawnStone();
                        gameTimer.start();
                        clockTimer.start(); 
                    } else {
                        handleGameOver("The puzzle defeated you! The creature has caught you.");
                    }
                });
            }
        } catch (Exception ex) {
            gameTimer.start();
            clockTimer.start();
        }
    }

    private void handleGameOver(String message) {
        gameTimer.stop();
        clockTimer.stop();
        
        // Save score using the 'db' variable defined at the top
        db.saveScore(username, stonesCollected, secondsElapsed);

        String summary = "--- SESSION SUMMARY ---\n" +
                         "Status: " + message + "\n" +
                         "Stones Collected: " + stonesCollected + "\n" +
                         "Time Survived: " + secondsElapsed + " seconds\n\n" +
                         "Your score has been uploaded to the leaderboard.";
        
        JOptionPane.showMessageDialog(this, summary, "THE LAST RUN - RESULTS", JOptionPane.INFORMATION_MESSAGE);

        this.dispose();
        new Dashboard(username);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) keys[code] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) keys[code] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}