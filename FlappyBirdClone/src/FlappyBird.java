import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    // Game constants
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int BIRD_SIZE = 40;
    private static final int GROUND_HEIGHT = 50;
    private static final Color SKY_COLOR = new Color(135, 206, 235);
    private static final Color GROUND_COLOR = new Color(139, 69, 19);
    private static final Color BIRD_COLOR = new Color(255, 215, 0);
    private static final Color PIPE_COLOR = new Color(34, 139, 34);
    private static final Color PIPE_EDGE_COLOR = new Color(0, 100, 0);
    
    // Game variables
    private int birdY = HEIGHT / 2;
    private int velocity = 0;
    private int gravity = 1;
    private int jumpStrength = -15;
    private boolean gameOver = false;
    private boolean gameStarted = false;
    private int score = 0;
    private int highScore = 0;
    
    // Pipe variables
    private int pipeSpeed = 10;
    private int pipeGap = 200;
    private int timerDelay = 30;
    private ArrayList<Rectangle> pipes;
    private int pipeWidth = 70;
    private int pipeHeight;
    private Random random = new Random();
    
    // Animation variables
    private int birdWingPosition = 0;
    private int wingAnimationCounter = 0;
    private int cloudX = 0;
    private int cloud2X = WIDTH / 2;
    
    // Game timer
    private Timer timer;
    
    public FlappyBird(int pipeSpeed, int timerDelay, int pipeGap, int jumpStrength) {
        this.pipeSpeed = pipeSpeed;
        this.timerDelay = timerDelay;
        this.pipeGap = pipeGap;
        this.jumpStrength = jumpStrength;
        
        // Initialize pipes
        pipes = new ArrayList<>();
        addPipePair();
        
        // Load high score
        loadHighScore();
        
        // Set up the frame
        JFrame frame = new JFrame("Flappy Bird - Advanced Edition");
        frame.setSize(WIDTH, HEIGHT);
        frame.setResizable(false);
        frame.add(this);
        frame.addKeyListener(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Center the window
        frame.setVisible(true);
        
        // Start game timer
        timer = new Timer(timerDelay, this);
        timer.start();
    }
    
    private void addPipePair() {
        pipeHeight = random.nextInt(HEIGHT - GROUND_HEIGHT - pipeGap - 200) + 100;
        pipes.add(new Rectangle(WIDTH, 0, pipeWidth, pipeHeight)); // Top pipe
        pipes.add(new Rectangle(WIDTH, pipeHeight + pipeGap, pipeWidth, HEIGHT - pipeHeight - pipeGap - GROUND_HEIGHT)); // Bottom pipe
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameStarted && !gameOver) {
            // Update bird position
            birdY += velocity;
            velocity += gravity;
            
            // Update pipes
            for (Rectangle pipe : pipes) {
                pipe.x -= pipeSpeed;
            }
            
            // Add new pipes when needed
            if (pipes.get(pipes.size() - 1).x < WIDTH - 300) {
                addPipePair();
            }
            
            // Remove off-screen pipes and increase score
            if (pipes.get(0).x + pipeWidth < 0) {
                pipes.remove(0);
                pipes.remove(0);
                score++;
                playScoreSound();
            }
            
            // Check collisions
            Rectangle birdRect = new Rectangle(100, birdY, BIRD_SIZE, BIRD_SIZE);
            for (Rectangle pipe : pipes) {
                if (pipe.intersects(birdRect)) {
                    gameOver = true;
                    playCollisionSound();
                }
            }
            
            // Check ground collision
            if (birdY > HEIGHT - GROUND_HEIGHT - BIRD_SIZE || birdY < 0) {
                gameOver = true;
                playCollisionSound();
            }
            
            // Update animations
            wingAnimationCounter = (wingAnimationCounter + 1) % 10;
            if (wingAnimationCounter == 0) {
                birdWingPosition = (birdWingPosition + 1) % 3;
            }
            
            // Update clouds
            cloudX = (cloudX + 1) % WIDTH;
            cloud2X = (cloud2X + 1) % WIDTH;
            
            repaint();
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw sky
        g.setColor(SKY_COLOR);
        g.fillRect(0, 0, WIDTH, HEIGHT - GROUND_HEIGHT);
        
        // Draw clouds
        drawCloud(g, cloudX, 100);
        drawCloud(g, cloud2X, 50);
        
        // Draw pipes
        for (Rectangle pipe : pipes) {
            drawPipe(g, pipe);
        }
        
        // Draw ground
        g.setColor(GROUND_COLOR);
        g.fillRect(0, HEIGHT - GROUND_HEIGHT, WIDTH, GROUND_HEIGHT);
        
        // Draw bird
        drawBird(g, 100, birdY);
        
        // Draw score
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("Score: " + score, 30, 50);
        g.drawString("High Score: " + highScore, WIDTH - 250, 50);
        
        // Game messages
        if (gameOver) {
            drawCenteredString(g, "GAME OVER", new Font("Arial", Font.BOLD, 60), WIDTH/2, HEIGHT/2 - 50);
            drawCenteredString(g, "Score: " + score, new Font("Arial", Font.BOLD, 40), WIDTH/2, HEIGHT/2 + 20);
            drawCenteredString(g, "Press SPACE to Restart", new Font("Arial", Font.BOLD, 30), WIDTH/2, HEIGHT/2 + 80);
            
            if (score > highScore) {
                highScore = score;
                saveHighScore();
                drawCenteredString(g, "NEW HIGH SCORE!", new Font("Arial", Font.BOLD, 30), WIDTH/2, HEIGHT/2 + 120);
            }
        }
        
        if (!gameStarted) {
            drawCenteredString(g, "FLAPPY BIRD", new Font("Arial", Font.BOLD, 60), WIDTH/2, HEIGHT/2 - 100);
            drawCenteredString(g, "Press SPACE to Start", new Font("Arial", Font.BOLD, 30), WIDTH/2, HEIGHT/2);
            drawCenteredString(g, "Use SPACE to Flap", new Font("Arial", Font.PLAIN, 20), WIDTH/2, HEIGHT/2 + 50);
        }
    }
    
    private void drawBird(Graphics g, int x, int y) {
        // Body
        g.setColor(BIRD_COLOR);
        g.fillOval(x, y, BIRD_SIZE, BIRD_SIZE);
        
        // Wing animation
        int[] wingX = {x + BIRD_SIZE/2, x + BIRD_SIZE/2, x + BIRD_SIZE};
        int[] wingY = {y + BIRD_SIZE/2, y + BIRD_SIZE/2, y + BIRD_SIZE/2 - 5 + birdWingPosition * 5};
        g.fillPolygon(wingX, wingY, 3);
        
        // Eye
        g.setColor(Color.WHITE);
        g.fillOval(x + BIRD_SIZE - 15, y + 10, 10, 10);
        g.setColor(Color.BLACK);
        g.fillOval(x + BIRD_SIZE - 13, y + 12, 6, 6);
        
        // Beak
        g.setColor(Color.ORANGE);
        int[] beakX = {x + BIRD_SIZE, x + BIRD_SIZE + 15, x + BIRD_SIZE};
        int[] beakY = {y + BIRD_SIZE/2 - 5, y + BIRD_SIZE/2, y + BIRD_SIZE/2 + 5};
        g.fillPolygon(beakX, beakY, 3);
    }
    
    private void drawPipe(Graphics g, Rectangle pipe) {
        // Pipe body
        g.setColor(PIPE_COLOR);
        g.fillRect(pipe.x, pipe.y, pipe.width, pipe.height);
        
        // Pipe edges
        g.setColor(PIPE_EDGE_COLOR);
        g.fillRect(pipe.x - 5, pipe.y, 5, pipe.height); // Left edge
        g.fillRect(pipe.x + pipe.width, pipe.y, 5, pipe.height); // Right edge
        
        // Top/bottom edge for top/bottom pipes
        if (pipe.y == 0) { // Top pipe
            g.fillRect(pipe.x - 5, pipe.y + pipe.height - 20, pipe.width + 10, 20);
        } else { // Bottom pipe
            g.fillRect(pipe.x - 5, pipe.y, pipe.width + 10, 20);
        }
    }
    
    private void drawCloud(Graphics g, int x, int y) {
        g.setColor(Color.WHITE);
        g.fillOval(x, y, 60, 40);
        g.fillOval(x + 20, y - 10, 60, 40);
        g.fillOval(x + 40, y, 60, 40);
        g.fillOval(x + 20, y + 10, 60, 40);
    }
    
    private void drawCenteredString(Graphics g, String text, Font font, int x, int y) {
        FontMetrics metrics = g.getFontMetrics(font);
        int textX = x - metrics.stringWidth(text) / 2;
        int textY = y - metrics.getHeight() / 2 + metrics.getAscent();
        g.setFont(font);
        g.drawString(text, textX, textY);
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!gameStarted) {
                gameStarted = true;
            }
            
            if (gameOver) {
                resetGame();
            }
            
            if (!gameOver) {
                velocity = jumpStrength;
                playFlapSound();
            }
        }
    }
    
    private void resetGame() {
        gameOver = false;
        score = 0;
        birdY = HEIGHT / 2;
        velocity = 0;
        pipes.clear();
        addPipePair();
    }
    
    // Sound effects (simulated - would use AudioClip in a real implementation)
    private void playFlapSound() {
        // In a real implementation, you would play a sound here
        System.out.println("Flap sound!");
    }
    
    private void playScoreSound() {
        // In a real implementation, you would play a sound here
        System.out.println("Score sound!");
    }
    
    private void playCollisionSound() {
        // In a real implementation, you would play a sound here
        System.out.println("Collision sound!");
    }
    
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    
    public static void main(String[] args) {
        // Difficulty selection using JOptionPane
        String[] options = {"Easy", "Medium", "Hard"};
        String choice = (String) JOptionPane.showInputDialog(
            null, 
            "Choose Difficulty", 
            "Flappy Bird - Difficulty Selection",
            JOptionPane.QUESTION_MESSAGE, 
            null, 
            options, 
            options[1]
        );
        
        if (choice != null) {
            switch (choice) {
                case "Easy":
                    new FlappyBird(5, 40, 250, -12); // Slower pipes, bigger gap, slower game
                    break;
                case "Medium":
                    new FlappyBird(8, 30, 200, -15); // Balanced difficulty
                    break;
                case "Hard":
                    new FlappyBird(12, 20, 150, -18); // Faster pipes, smaller gap, faster game
                    break;
            }
        }
    }
    
    // Load high score from file
    private void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader("highscore.txt"))) {
            highScore = Integer.parseInt(reader.readLine());
        } catch (IOException | NumberFormatException e) {
            highScore = 0;
        }
    }
    
    // Save high score to file
    private void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("highscore.txt"))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}