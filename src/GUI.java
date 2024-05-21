import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

public class GUI extends JFrame {
    private JButton stressTestButton;
    private JButton benchmarkButton;
    private JButton settingsButton;
    private JButton aboutButton;
    private JButton stressCPUButton;
    private JButton stressRAMButton;
    private JButton benchmarkCPUButton;
    private JButton benchmarkCPU2Button;
    private JButton benchmarkRAMButton;
    private JButton goBackStep1Button;
    private JButton goBackMainMenuButton;
    private JButton saveSettingsButton;
    private JButton openCPUDatabaseButton;
    private JButton openRAMDatabaseButton;
    private JButton stressYourselfButton;
    private JPanel mainMenuPanel;
    private JPanel step1PanelStress;
    private JPanel step2PanelStress;
    private JPanel step1PanelBenchmark;
    private JPanel step2PanelBenchmark;
    private JPanel settingsPanel;
    private JPanel resultsPanel;
    private JLabel timerLabel;
    private JProgressBar progressBar;
    private Process processCPUstress;
    private Timer timer;
    private int elapsedTime;
    private static final String SettingsFileName="settings.txt";
    private ArrayList<String> keys = new ArrayList<>();
    private ArrayList<String> values = new ArrayList<>();

    private ArrayList<JComponent> valueComponents = new ArrayList<>();
    private Process processCPUbenchmark;
    private Process processRAMbenchmark;

    private String SingleThreadScore;
    private String MultiThreadScore;
    private String ramScore;


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
        initializeStep1PanelStress();
        initializeStep2PanelStress();
        initializeStep1PanelBenchmark();
        initializeStep2PanelBenchmark();
        initializeSettingsPanel();
        initializeResultsPanel();


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
        Color buttonColor = new Color(50, 50, 50); // Gray
        Color textColor = new Color(255, 255, 255);
        Dimension buttonSize = new Dimension(750, 400);

        stressTestButton = createStyledButton("Stress Test", buttonFont, buttonColor, buttonSize, textColor);
        benchmarkButton = createStyledButton("Benchmark", buttonFont, buttonColor, buttonSize, textColor);
        settingsButton = createStyledButton("Settings", buttonFont, buttonColor, buttonSize, textColor);
        openCPUDatabaseButton = createStyledButton("CPUdatabase",buttonFont,buttonColor,buttonSize,textColor);
        openRAMDatabaseButton = createStyledButton("RAMdatabase",buttonFont,buttonColor,buttonSize,textColor);
        aboutButton = createStyledButton("About", buttonFont, buttonColor, buttonSize, textColor);

        stressTestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStepStress1();
            }
        });

        benchmarkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStepBenchmark1();
            }
        });

        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSettings();
            }
        });

        openCPUDatabaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCPUDatabase();
            }
        });

        openRAMDatabaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRAMDatabase();
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
        mainMenuPanel.add(openCPUDatabaseButton);
        mainMenuPanel.add(Box.createVerticalStrut(10)); // Add spacing
        mainMenuPanel.add(openRAMDatabaseButton);
        mainMenuPanel.add(Box.createVerticalStrut(10)); // Add spacing
        mainMenuPanel.add(aboutButton);

        // Align button panel to the left side
        add(mainMenuPanel, BorderLayout.WEST);
    }

    private void initializeStep1PanelStress() {
        step1PanelStress = new JPanel();
        step1PanelStress.setOpaque(false);
        step1PanelStress.setLayout(new BoxLayout(step1PanelStress, BoxLayout.Y_AXIS));

        Font buttonFont = new Font("Arial", Font.BOLD, 25);
        Color buttonColor = new Color(50, 50, 50); // Gray
        Color textColor = new Color(255, 255, 255);
        Dimension buttonSize = new Dimension(750, 350);

        stressCPUButton = createStyledButton("Stress CPU", buttonFont, buttonColor, buttonSize, textColor);
        stressCPUButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStepStress2();
                // Start FileCompresserBenchmark in stress mode
                try {
                    startCPUstress();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                startTimer();
            }
        });

        stressRAMButton = createStyledButton("Stress RAM", buttonFont, buttonColor, buttonSize, textColor);
        stressRAMButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStepStress2();
                try {
                    startRAMStressTest(); // Call method to start RAM stress test
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                startTimer();
            }


        });

        stressYourselfButton = createStyledButton("Stress Yourself",buttonFont,buttonColor,buttonSize,textColor);

        stressYourselfButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openStressVideo("https://youtu.be/dLxPIOxgkM0?si=6jRLUZ-4fN4867Rj");
            }
        });

        goBackStep1Button = createStyledButton("Go Back", buttonFont, buttonColor, buttonSize, textColor);
        goBackStep1Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMainMenu();
            }
        });

        step1PanelStress.add(Box.createVerticalStrut(20)); // Add spacing
        step1PanelStress.add(stressCPUButton);
        step1PanelStress.add(Box.createVerticalStrut(20)); // Add spacing
        step1PanelStress.add(stressRAMButton);
        step1PanelStress.add(Box.createVerticalStrut(20)); // Add spacing
        step1PanelStress.add(stressYourselfButton);
        step1PanelStress.add(Box.createVerticalStrut(320)); // Add spacing
        step1PanelStress.add(goBackStep1Button);
    }

    private void initializeStep2PanelStress() {
        step2PanelStress = new JPanel();
        step2PanelStress.setOpaque(false);
        step2PanelStress.setLayout(new BoxLayout(step2PanelStress, BoxLayout.Y_AXIS));

        Font buttonFont = new Font("Arial", Font.BOLD, 25);
        Color buttonColor = new Color(50, 50, 50); // Gray
        Color textColor = new Color(255, 255, 255);
        Dimension buttonSize = new Dimension(750, 350);

        goBackMainMenuButton = createStyledButton("Stop StressTest", buttonFont, buttonColor, buttonSize, textColor);
        goBackMainMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopTimer();
                showStepStress1();
            }
        });

        timerLabel = new JLabel("", SwingConstants.CENTER); // Center the timer label
        timerLabel.setFont(new Font("Arial", Font.BOLD, 30));
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center the text horizontally

        step2PanelStress.add(timerLabel);
        step2PanelStress.add(Box.createVerticalStrut(20)); // Add spacing
        step2PanelStress.add(goBackMainMenuButton);
    }


    private void initializeStep1PanelBenchmark() {
        step1PanelBenchmark = new JPanel();
        step1PanelBenchmark.setOpaque(false);
        step1PanelBenchmark.setLayout(new BoxLayout(step1PanelBenchmark, BoxLayout.Y_AXIS));

        Font buttonFont = new Font("Arial", Font.BOLD, 25);
        Color buttonColor = new Color(50, 50, 50); // Gray
        Color textColor = new Color(255, 255, 255);
        Dimension buttonSize = new Dimension(750, 300);

        benchmarkCPUButton = createStyledButton("Benchmark CPU 1", buttonFont, buttonColor, buttonSize, textColor);
        benchmarkCPUButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStepBenchmark2();
                try {
                    startCPUbenchmark("-benchmark1");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                startProgressBar(); // Start progress bar
            }

        });

        benchmarkCPU2Button = createStyledButton("Benchmark CPU 2", buttonFont, buttonColor, buttonSize, textColor);
        benchmarkCPU2Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStepBenchmark2();
                try {
                    startCPUbenchmark("-benchmark2");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                startProgressBar(); // Start progress bar
            }
        });

        benchmarkRAMButton = createStyledButton("Benchmark RAM", buttonFont, buttonColor, buttonSize, textColor);
        benchmarkRAMButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStepBenchmark2();
                try {
                    startRAMBenchmarkTest(); // Call method to start RAM benchmark instead of stress test
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                startProgressBar(); // Start progress bar
            }
        });

        goBackStep1Button = createStyledButton("Go Back", buttonFont, buttonColor, buttonSize, textColor);
        goBackStep1Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMainMenu();
            }
        });

        step1PanelBenchmark.add(Box.createVerticalStrut(20)); // Add spacing
        step1PanelBenchmark.add(benchmarkCPUButton);
        step1PanelBenchmark.add(Box.createVerticalStrut(20)); // Add spacing
        step1PanelBenchmark.add(benchmarkCPU2Button);
        step1PanelBenchmark.add(Box.createVerticalStrut(20)); // Add spacing
        step1PanelBenchmark.add(benchmarkRAMButton);
        step1PanelBenchmark.add(Box.createVerticalStrut(320)); // Add spacing
        step1PanelBenchmark.add(goBackStep1Button);
    }
    private void initializeStep2PanelBenchmark() {
        step2PanelBenchmark = new JPanel();
        step2PanelBenchmark.setOpaque(false);
        step2PanelBenchmark.setLayout(new BoxLayout(step2PanelBenchmark, BoxLayout.Y_AXIS));

        Font buttonFont = new Font("Arial", Font.BOLD, 25);
        Color buttonColor = new Color(50, 50, 50); // Gray
        Color textColor = new Color(255, 255, 255);
        Dimension buttonSize = new Dimension(750, 350);

        goBackStep1Button = createStyledButton("Stop Test", buttonFont, buttonColor, buttonSize, textColor);
        goBackStep1Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopProgressBar(); // Stop progress bar
                showStepBenchmark1();
            }
        });
        // Initialize progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false); // Disable progress bar by default
        step2PanelBenchmark.add(progressBar); // Add the progress bar
        step2PanelBenchmark.add(Box.createVerticalStrut(450)); // Add spacing
        step2PanelBenchmark.add(goBackStep1Button);
    }

    private void initializeSettingsPanel(){
        settingsPanel = new JPanel();
        settingsPanel.setOpaque(false);
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));

        Font buttonFont = new Font("Arial", Font.BOLD, 25);
        Color buttonColor = new Color(50, 50, 50); // Gray
        Color textColor = new Color(255, 255, 255);
        Dimension buttonSize = new Dimension(750, 50);
        loadSettings(buttonFont,textColor,buttonColor);

        goBackMainMenuButton = createStyledButton("Go Back", buttonFont, buttonColor, buttonSize, textColor);
        goBackMainMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMainMenu();
            }
        });
        saveSettingsButton = createStyledButton("Save Settings",buttonFont,buttonColor,buttonSize,textColor);
        saveSettingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveSettings();
            }
        });
        settingsPanel.add(Box.createVerticalStrut(210));
        settingsPanel.add(saveSettingsButton);
        settingsPanel.add(Box.createVerticalStrut(20)); // Add spacing
        settingsPanel.add(goBackMainMenuButton);

    }

    private void initializeResultsPanel(){
        resultsPanel = new JPanel();
        resultsPanel.setOpaque(false);
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));

        Font buttonFont = new Font("Arial", Font.BOLD, 25);
        Color buttonColor = new Color(50, 50, 50); // Gray
        Color textColor = new Color(255, 255, 255);
        Dimension buttonSize = new Dimension(100, 50);

        goBackMainMenuButton = createStyledButton("Go Back", buttonFont, buttonColor, buttonSize, textColor);
        goBackMainMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMainMenu();
            }
        });

        JLabel title=new JLabel("Scores");
        JLabel singleScore=new JLabel("Single Thread Score: "+SingleThreadScore);
        JLabel multiScore=new JLabel("Multi Thread Score: "+MultiThreadScore);
        StyledComponent(title,buttonFont,textColor,buttonColor);
        StyledComponent(singleScore,buttonFont,textColor,buttonColor);
        StyledComponent(multiScore,buttonFont,textColor,buttonColor);

        resultsPanel.add(title);
        resultsPanel.add(singleScore);
        resultsPanel.add(multiScore);
        resultsPanel.add(Box.createVerticalStrut(20)); // Add spacing
        resultsPanel.add(goBackMainMenuButton);
    }

    private void loadSettings(Font font,Color textColor,Color backgroundColor){
        try {
            BufferedReader InputReader = new BufferedReader(new FileReader(SettingsFileName));
            String line;
            BenchmarkSettings results = new BenchmarkSettings();
            while ((line = InputReader.readLine()) != null) {
                StringTokenizer stringTokenizer = new StringTokenizer(line, "=");
                keys.add(stringTokenizer.nextToken());
                values.add(stringTokenizer.nextToken());
            }
            for (int i = 0; i < keys.size(); i++) {
                JLabel setting = new JLabel(keys.get(i));
                JComponent component;
                if ("true".equalsIgnoreCase(values.get(i)) || "false".equalsIgnoreCase(values.get(i))) {
                    JCheckBox checkBox = new JCheckBox();
                    checkBox.setSelected(Boolean.parseBoolean(values.get(i)));
                    component = checkBox;
                } else {
                    component = new JTextField(values.get(i), 20);
                }
                StyledComponent(setting,font,textColor,backgroundColor);
                StyledComponent(component,font,textColor,backgroundColor);
                valueComponents.add(component);
                settingsPanel.add(setting);
                settingsPanel.add(component);
            }
        }
        catch(IOException e){
            e.getStackTrace();
        }
    }
    private void setCurrentSettings(){
        for(int i=0;i<keys.size();i++){
            JComponent currentComponent=valueComponents.get(i);
            String value="";
            if (currentComponent instanceof JTextField) {
                value=((JTextField) currentComponent).getText();
            } else if (currentComponent instanceof JCheckBox) {
                value=String.valueOf(((JCheckBox) currentComponent).isSelected());
            }
            values.set(i, value);
        }
    }
    private void saveSettings(){
        try{
            setCurrentSettings();
            PrintWriter settingsPrinter= new PrintWriter(new FileWriter(SettingsFileName));
            for(int i=0;i<keys.size();i++){
                settingsPrinter.println(keys.get(i)+"="+values.get(i));
            }
            System.out.println("Settings have been saved");
            settingsPrinter.close();
        }
        catch(IOException e){
            e.getStackTrace();
        }
    }

    private void StyledComponent(JComponent component,Font font,Color textColor, Color backgroundColor){
        component.setFont(font);
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setForeground(textColor);
        component.setBackground(backgroundColor);
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

    private void openStressVideo(String urlString){
        try {
            Desktop.getDesktop().browse(new URL(urlString).toURI());
        } catch (Exception e) {
            e.printStackTrace();
        };
    }

    private void openCPUDatabase() {
        try {
            Desktop.getDesktop().open(new File("DatabaseCPU.csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openRAMDatabase() {
        try {
            Desktop.getDesktop().open(new File("databaseRAM.csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startTimer() {
        timerLabel.setText("00:00"); // Set the timer text to 0 to display the correct time during the first second
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

    private void startProgressBar() {
        progressBar.setVisible(true);
    }

    private void stopProgressBar() {
        progressBar.setVisible(false);
    }

    private void startCPUbenchmark(String benchmarkCPU) throws IOException{
        SwingWorker<Void,Integer> worker= new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                //ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", "FileCompresserBenchmark.jar", "FileCompresserBenchmark", benchmarkCPU);
                ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", "./out/production/FileCompresserBenchmark", "FileCompresserBenchmark", benchmarkCPU);
                processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
                processCPUbenchmark = processBuilder.start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(processCPUbenchmark.getInputStream()))) {
                    String line;
                    String lastLine=" ";
                    while ((line = reader.readLine()) != null) {
                        try {
                            lastLine=line;
                            int progress = Integer.parseInt(line.trim());
                            publish(progress);
                        } catch (NumberFormatException e) {
                            // Ignore lines that are not numbers
                        }
                    }
                    String[] Results=lastLine.split(",");
                    SingleThreadScore=Results[0];
                    MultiThreadScore=Results[1];
                }
                processCPUbenchmark.waitFor();
                return null;
            }
            @Override
            protected void process(java.util.List<Integer> chunks) {
                for (int progress : chunks) {
                    progressBar.setValue(progress);
                }
            }
            @Override
            protected void done() {
                progressBar.setValue(100);
                initializeResultsPanel();
                showResults();
            }
        };

        worker.execute();
    }

    private void startCPUstress() throws IOException {
        // Start as a process so it can be easily stop
        //ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", "FileCompresserBenchmark.jar", "FileCompresserBenchmark", "-stress");
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", "./out/production/FileCompresserBenchmark", "FileCompresserBenchmark", "-stress");
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processCPUstress = processBuilder.start();
    }

    private void startRAMStressTest() throws IOException {
        // Start as a process so it can be easily stop
        //ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", "FileCompresserBenchmark.jar", "FileCompresserBenchmark", "-stress");
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", "./out/production/FileCompresserBenchmark", "RAMStressTest", "-stress");
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processCPUstress = processBuilder.start();
    }

    private void startRAMBenchmarkTest() throws IOException {
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", "./out/production/FileCompresserBenchmark", "RAMStressTest");
                processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
                Process processRAMbenchmark = processBuilder.start();
                System.out.println("Started process");

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(processRAMbenchmark.getInputStream()))) {
                    String line;
                    String lastLine = " ";
                    while ((line = reader.readLine()) != null) {
                        try {
                            lastLine = line;
                            int progress = Integer.parseInt(line.trim());
                            publish(progress);
                        } catch (NumberFormatException e) {
                            // Ignore lines that are not numbers
                        }
                    }
                    String[] results = lastLine.split(" ");
                    if (results.length == 2) {
                        ramScore = results[1];
                    } else {
                        throw new IOException("Invalid output format from the RAM stress test");
                    }
                }

                processRAMbenchmark.waitFor();
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                // Process chunks to update the UI with progress if needed
                for (int progress : chunks) {
                    progressBar.setValue(progress);
                }
            }

            @Override
            protected void done() {
                progressBar.setValue(100);
                initializeResultsPanelRam();
                showResults();
            }
        };

        worker.execute();
    }

    private void initializeResultsPanelRam(){
        resultsPanel = new JPanel();
        resultsPanel.setOpaque(false);
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));

        Font buttonFont = new Font("Arial", Font.BOLD, 25);
        Color buttonColor = new Color(50, 50, 50); // Gray
        Color textColor = new Color(255, 255, 255);
        Dimension buttonSize = new Dimension(200, 50);

        goBackMainMenuButton = createStyledButton("Go Back", buttonFont, buttonColor, buttonSize, textColor);
        goBackMainMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMainMenu();
            }
        });

        JLabel ramScoreLabel=new JLabel("Score: "+ramScore);
        StyledComponent(ramScoreLabel,buttonFont,textColor,buttonColor);

        resultsPanel.add(ramScoreLabel);
        resultsPanel.add(Box.createVerticalStrut(20)); // Add spacing
        resultsPanel.add(goBackMainMenuButton);
    }

    private void stopAllProcesses() {
        if (processCPUstress!=null) {
            processCPUstress.destroy();
        }
        if (processCPUbenchmark!=null) {
            processCPUbenchmark.destroy();
        }
        System.out.println("All processes had been stopped");
    }

    private void showMainMenu() {
        stopTimer();
        stopAllProcesses();
        stopProgressBar(); // Stop progress bar if active
        remove(step1PanelStress);
        remove(step2PanelStress);
        remove(step1PanelBenchmark);
        remove(step2PanelBenchmark);
        remove(settingsPanel);
        remove(resultsPanel);
        add(mainMenuPanel, BorderLayout.WEST);
        revalidate();
        repaint();
    }

    private void showStepStress1() {
        stopTimer();
        stopAllProcesses();
        stopProgressBar();
        remove(mainMenuPanel);
        remove(step2PanelStress);
        add(step1PanelStress, BorderLayout.WEST);
        revalidate();
        repaint();
    }

    private void showStepStress2() {
        remove(mainMenuPanel);
        remove(step1PanelStress);
        add(step2PanelStress, BorderLayout.WEST);
        revalidate();
        repaint();
    }

    private void showStepBenchmark1(){
        stopTimer();
        stopAllProcesses();
        stopProgressBar();
        remove(mainMenuPanel);
        remove(step2PanelBenchmark);
        add(step1PanelBenchmark, BorderLayout.WEST);
        revalidate();
        repaint();
    }

    private void showStepBenchmark2(){
        remove(mainMenuPanel);
        remove(step1PanelBenchmark);
        add(step2PanelBenchmark, BorderLayout.WEST);
        revalidate();
        repaint();
    }

    private void showSettings(){
        stopTimer();
        stopAllProcesses();
        stopProgressBar();
        remove(mainMenuPanel);
        add(settingsPanel, BorderLayout.WEST);
        revalidate();
        repaint();
    }

    private void showResults(){
        stopTimer();
        stopAllProcesses();
        stopProgressBar();
        remove(step2PanelBenchmark);
        add(resultsPanel,BorderLayout.WEST);
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
