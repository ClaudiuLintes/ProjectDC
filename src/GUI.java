import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GUI extends JFrame {
    private JButton stressTestButton;
    private JButton benchmarkButton;
    private JButton settingsButton;
    private JButton aboutButton;
    private JButton stressCPUButton;
    private JButton stressRAMButton;
    private JButton goBackStep1Button;
    private JButton goBackMainMenuButton;
    private JPanel mainMenuPanel;
    private JPanel step1Panel;
    private JPanel step2Panel;
    private JPanel buttonPanel;
    private JLabel timerLabel;
    private Timer timer;
    private int elapsedTime;

    public GUI() {
        setPreferredSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set application icon
        try {
            setIconImage(ImageIO.read(new File("logo.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load background image
        BufferedImage backgroundImage = null;
        try {
            backgroundImage = ImageIO.read(new File("background1.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImagePanel imagePanel = new ImagePanel(backgroundImage);
        setContentPane(imagePanel);

        // Initialize panels
        initializeMainMenuPanel();
        initializeStep1Panel();
        initializeStep2Panel();

        // Show main menu by default
        showMainMenu();

        pack();
        setLocationRelativeTo(null); // Center the frame
        setResizable(false); // Fixed size

        setVisible(true);
    }

    private void initializeMainMenuPanel() {
        mainMenuPanel = new JPanel();
        mainMenuPanel.setOpaque(false);
        mainMenuPanel.setLayout(new BoxLayout(mainMenuPanel, BoxLayout.Y_AXIS));

        // Set button properties
        Font buttonFont = new Font("Arial", Font.BOLD, 25);
        Color buttonColor = new Color(50, 50, 50); // Light pink
        Color textColor = new Color(255, 255, 255);
        Dimension buttonSize = new Dimension(750, 400);

        stressTestButton = createStyledButton("Stress Test", buttonFont, buttonColor, buttonSize, textColor);
        benchmarkButton = createStyledButton("Benchmark", buttonFont, buttonColor, buttonSize, textColor);
        settingsButton = createStyledButton("Settings", buttonFont, buttonColor, buttonSize, textColor);
        aboutButton = createStyledButton("About", buttonFont, buttonColor, buttonSize, textColor);

        stressTestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStep1();
            }
        });

        aboutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openPDFDocumentation();
            }
        });

        mainMenuPanel.add(Box.createVerticalStrut(10)); // Add spacing
        mainMenuPanel.add(stressTestButton);
        mainMenuPanel.add(Box.createVerticalStrut(10)); // Add spacing
        mainMenuPanel.add(benchmarkButton);
        mainMenuPanel.add(Box.createVerticalStrut(10)); // Add spacing
        mainMenuPanel.add(settingsButton);
        mainMenuPanel.add(Box.createVerticalStrut(10)); // Add spacing
        mainMenuPanel.add(aboutButton);

        // Align button panel to the left side
        add(mainMenuPanel, BorderLayout.WEST);
    }

    private void initializeStep1Panel() {
        step1Panel = new JPanel();
        step1Panel.setOpaque(false);
        step1Panel.setLayout(new BoxLayout(step1Panel, BoxLayout.Y_AXIS));

        Font buttonFont = new Font("Arial", Font.BOLD, 25);
        Color buttonColor = new Color(50, 50, 50); // Light pink
        Color textColor = new Color(255, 255, 255);
        Dimension buttonSize = new Dimension(750, 350);

        stressCPUButton = createStyledButton("Stress CPU", buttonFont, buttonColor, buttonSize, textColor);
        stressCPUButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStep2();
                startTimer();
            }
        });

        stressRAMButton = createStyledButton("Stress RAM", buttonFont, buttonColor, buttonSize, textColor);
        stressRAMButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStep2();
                startTimer();
            }
        });

        goBackStep1Button = createStyledButton("Go Back", buttonFont, buttonColor, buttonSize, textColor);
        goBackStep1Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMainMenu();
            }
        });

        step1Panel.add(Box.createVerticalStrut(20)); // Add spacing
        step1Panel.add(stressCPUButton);
        step1Panel.add(Box.createVerticalStrut(20)); // Add spacing
        step1Panel.add(stressRAMButton);
        step1Panel.add(Box.createVerticalStrut(380)); // Add spacing
        step1Panel.add(goBackStep1Button);
    }

    private void initializeStep2Panel() {
        step2Panel = new JPanel();
        step2Panel.setOpaque(false);
        step2Panel.setLayout(new BoxLayout(step2Panel, BoxLayout.Y_AXIS));

        Font buttonFont = new Font("Arial", Font.BOLD, 25);
        Color buttonColor = new Color(50, 50, 50); // Light pink
        Color textColor = new Color(255, 255, 255);
        Dimension buttonSize = new Dimension(750, 350);

        goBackMainMenuButton = createStyledButton("Go Back", buttonFont, buttonColor, buttonSize, textColor);
        goBackMainMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopTimer();
                showStep1();
            }
        });

        timerLabel = new JLabel("", SwingConstants.CENTER); // Center the timer label
        timerLabel.setFont(new Font("Arial", Font.BOLD, 30));
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center the text horizontally
        step2Panel.add(timerLabel);
        step2Panel.add(Box.createVerticalStrut(470)); // Add spacing
        step2Panel.add(goBackMainMenuButton);
    }

    private JButton createStyledButton(String text, Font font, Color color, Dimension size, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setForeground(textColor);
        button.setBackground(color);
        button.setPreferredSize(size);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setBorderPainted(true); // Ensure border is painted
        button.setOpaque(true); // Ensure button is opaque
        button.setFocusPainted(false);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        return button;
    }

    private void openPDFDocumentation() {
        try {
            Desktop.getDesktop().open(new File("ProjectDocumentation.pdf"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startTimer() {
        elapsedTime = 0;
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                elapsedTime++;
                updateTimerLabel();
            }
        });
        timer.start();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    private void updateTimerLabel() {
        int minutes = elapsedTime / 60;
        int seconds = elapsedTime % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void showMainMenu() {
        stopTimer();
        remove(step1Panel);
        remove(step2Panel);
        add(mainMenuPanel, BorderLayout.WEST);
        revalidate();
        repaint();
    }

    private void showStep1() {
        stopTimer();
        remove(mainMenuPanel);
        remove(step2Panel);
        add(step1Panel, BorderLayout.WEST);
        revalidate();
        repaint();
    }

    private void showStep2() {
        remove(mainMenuPanel);
        remove(step1Panel);
        add(step2Panel, BorderLayout.WEST);
        revalidate();
        repaint();
    }

    // Custom JPanel for displaying background image
    class ImagePanel extends JPanel {
        private Image image;

        public ImagePanel(Image image) {
            this.image = image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GUI();
            }
        });
    }
}
