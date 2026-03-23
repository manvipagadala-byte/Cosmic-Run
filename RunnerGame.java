
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;

public class RunnerGame extends JFrame implements KeyListener {

    GamePanel panel = new GamePanel();

    public RunnerGame() {
        setTitle("Runner Game");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        add(panel);
        addKeyListener(this);
        setVisible(true);
    }

    public static void main(String[] args) {
        new RunnerGame();
    }

    // Controls
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            panel.jump();
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            panel.rollingKeyHeld = true;
        }
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            panel.rollingKeyHeld = false;
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    class GamePanel extends JPanel {

        Timer timer;
        Clip bgMusic;
        boolean rollingKeyHeld = false;

        BufferedImage background;
        int bgX = 0;

        int playerX = 80;
        int groundY = 260;
        int playerY = groundY;
        int velocityY = 0;

        int time = 0;
        int score = 0;
        int speed = 6;
        double spawnRate = 0.015;

        ArrayList<Obstacle> obstacles = new ArrayList<>();

        public GamePanel() {
            timer = new Timer(30, e -> updateGame());
            timer.start();

            try {
                background = ImageIO.read(new File("background.png"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            playBackgroundMusic();

        }

        void playBackgroundMusic() {
            try {
                AudioInputStream audioStream
                        = AudioSystem.getAudioInputStream(new File("music.wav"));
                bgMusic = AudioSystem.getClip();
                bgMusic.open(audioStream);
                bgMusic.loop(Clip.LOOP_CONTINUOUSLY);
                bgMusic.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void jump() {
            if (playerY == groundY) {
                velocityY = -18;
            }
        }

        void updateGame() {

            time++;

            // Scroll background
            bgX -= speed / 2;
            if (bgX <= -getWidth()) {
                bgX = 0;
            }

            // Smooth difficulty progression
            if (time % 600 == 0 && spawnRate < 0.05) {
                spawnRate += 0.002;
            }

            if (time % 1200 == 0 && speed < 8) {
                speed++;
            }

            // Gravity
            velocityY += 1;
            playerY += velocityY;

            if (playerY > groundY) {
                playerY = groundY;
                velocityY = 0;
            }

            // Spawn obstacles fairly
            if (obstacles.isEmpty() || obstacles.get(obstacles.size() - 1).x < 500) {
                if (Math.random() < spawnRate) {
                    obstacles.add(new Obstacle());
                }
            }

            // Move obstacles & collision
            Iterator<Obstacle> it = obstacles.iterator();
            while (it.hasNext()) {
                Obstacle o = it.next();
                o.x -= speed;

                Rectangle playerRect;
                if (rollingKeyHeld) {
                    playerRect = new Rectangle(playerX, playerY + 20, 40, 20);
                } else {
                    playerRect = new Rectangle(playerX, playerY, 40, 40);
                }

                Rectangle obsRect = new Rectangle(o.x, o.y, o.width, o.height);

                if (playerRect.intersects(obsRect)) {
                    timer.stop();
                    JOptionPane.showMessageDialog(this, "Game Over!\nScore: " + score);
                    System.exit(0);
                }

                if (!o.passed && o.x + o.width < playerX) {
                    score++;
                    o.passed = true;
                }

                if (o.x + o.width < 0) {
                    it.remove();
                }
            }

            repaint();
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Background
            g.drawImage(background, bgX, 0, getWidth(), getHeight(), null);
            g.drawImage(background, bgX + getWidth(), 0, getWidth(), getHeight(), null);

            // Score
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("Score: " + score, 20, 30);

            // Ground
            g.setColor(Color.GRAY);
            g.fillRect(0, groundY + 40, getWidth(), 10);

            // Player
            g.setColor(Color.PINK);
            if (rollingKeyHeld) {
                g.fillRect(playerX, playerY + 20, 40, 20);
            } else {
                g.fillRect(playerX, playerY, 40, 40);
            }

            // Obstacles
            g.setColor(Color.ORANGE);
            for (Obstacle o : obstacles) {
                g.fillRect(o.x, o.y, o.width, o.height);
            }
        }

        class Obstacle {

            int x = 700;
            int y;
            int width, height;
            boolean passed = false;

            Obstacle() {
                if (Math.random() < 0.5) {
                    width = 30;
                    height = 60;
                    y = groundY - 20;
                } else {
                    width = 60;
                    height = 30;
                    y = groundY - 25;
                }
            }
        }
    }
}
