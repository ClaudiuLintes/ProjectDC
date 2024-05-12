import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GUI extends JFrame {
    private JButton benchmarkButton;
    private JButton benchmarkCPUButton;
    private JButton benchmarkRAMButton;
    private JButton stressTestButton;
    private JButton stressCPUButton;
    private JButton stressRAMButton;
    private JButton settingsButton;
    private JButton aboutButton;

    public GUI() {
        setTitle("Performance Benchmark and Stress Test");
        setPreferredSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Load background image
        BufferedImage backgroundImage = null;
        try {
            backgroundImage = ImageIO.read(new File("gumball.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImagePanel imagePanel = new ImagePanel(backgroundImage);
        setContentPane(imagePanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(775, 550));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);

        // Set button properties
        Font buttonFont = new Font("Arial", Font.BOLD, 25);
        Color buttonColor = new Color(215, 182, 193); // Light pink
        Dimension buttonSize = new Dimension(200, 350);

        benchmarkButton = createStyledButton("Benchmark", buttonFont, buttonColor, buttonSize);
        benchmarkCPUButton = createStyledButton("Benchmark CPU", buttonFont, buttonColor, buttonSize);
        benchmarkRAMButton = createStyledButton("Benchmark RAM", buttonFont, buttonColor, buttonSize);
        benchmarkCPUButton.setVisible(false);
        benchmarkRAMButton.setVisible(false);

        stressTestButton = createStyledButton("Stress Test", buttonFont, buttonColor, buttonSize);
        stressCPUButton = createStyledButton("Stress CPU", buttonFont, buttonColor, buttonSize);
        stressRAMButton = createStyledButton("Stress RAM", buttonFont, buttonColor, buttonSize);
        stressCPUButton.setVisible(false);
        stressRAMButton.setVisible(false);

        settingsButton = createStyledButton("  Settings   ", buttonFont, buttonColor, buttonSize);
        aboutButton = createStyledButton("   About      ", buttonFont, buttonColor, buttonSize);
        aboutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openPDFDocumentation();
            }
        });

        // Add buttons to the panel
        buttonPanel.add(benchmarkButton);
        buttonPanel.add(benchmarkCPUButton);
        buttonPanel.add(benchmarkRAMButton);
        buttonPanel.add(Box.createVerticalStrut(20)); // Add spacing between sections
        buttonPanel.add(stressTestButton);
        buttonPanel.add(stressCPUButton);
        buttonPanel.add(stressRAMButton);
        buttonPanel.add(Box.createVerticalStrut(20)); // Add spacing between sections
        buttonPanel.add(settingsButton);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(aboutButton);

        // Align button panel to the left side
        add(buttonPanel, BorderLayout.WEST);

        pack();
        setLocationRelativeTo(null); // Center the frame
        setResizable(false); // Fixed size

        setVisible(true);
    }

    private JButton createStyledButton(String text, Font font, Color color, Dimension size) {
        JButton button = new JButton(text);
        button.setFont(font);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GUI();
            }
        });
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
}