package com.ved.filehash.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme;
import com.ved.filehash.constants.HashAlgorithm;
import com.ved.filehash.service.HashService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.Objects;
import java.util.prefs.Preferences;

public class FileHashGenerator extends JFrame {

    static boolean isDark = UIManager.getLookAndFeel().getName().toLowerCase().contains("dark");

    private enum Theme {SYSTEM, LIGHT, DARK, DRACULA}

    private static final Preferences PREFS =
            Preferences.userNodeForPackage(FileHashGenerator.class);

    private static final String PREF_THEME = "ui.theme";

    static {
        try {
            Theme theme = Theme.valueOf(
                    PREFS.get(PREF_THEME, Theme.SYSTEM.name())
            );

            switch (theme) {
                case LIGHT -> FlatLightLaf.setup();
                case DARK -> FlatDarkLaf.setup();
                case DRACULA -> FlatDraculaIJTheme.setup();
                case SYSTEM -> {
                    if (isDark)
                        FlatDarkLaf.setup();
                    else
                        FlatLightLaf.setup();
                }
            }

            UIManager.put("TitlePane.useWindowDecorations", true);
            UIManager.put("Component.arc", 12);
            UIManager.put("Button.arc", 12);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("defaultFont",
                    new Font("Segoe UI", Font.PLAIN, 13));

        } catch (Exception e) {
            System.err.println("Failed to initialize Look & Feel");
        }
    }


    private JTextField fileField;
    private JComboBox<HashAlgorithm> algoBox;
    private JTextArea resultArea;
    private JProgressBar progressBar;
    private File selectedFile;


    public FileHashGenerator() {
        setTitle("File Hash Generator");
        setSize(960, 540);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        Image icon = Toolkit.getDefaultToolkit()
                .getImage(getClass().getResource("/slack.png"));
        setIconImage(icon);
        initUI();
    }


    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        Dimension btnSize = new Dimension(140, 32);


        JPanel filePanel = new JPanel(new BorderLayout(10, 0));
        fileField = new JTextField();
        fileField.setEditable(false);

        JButton browseBtn = new JButton("Browse");
        browseBtn.setPreferredSize(btnSize);
        browseBtn.addActionListener(e -> chooseFile());

        filePanel.add(fileField, BorderLayout.CENTER);
        filePanel.add(browseBtn, BorderLayout.EAST);

        root.add(filePanel, BorderLayout.NORTH);


        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JPanel algoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        algoBox = new JComboBox<>(HashAlgorithm.values());
        algoBox.setPreferredSize(btnSize);

        JButton generateBtn = new JButton("Generate Hash");
        generateBtn.setPreferredSize(btnSize);
        generateBtn.addActionListener(e -> generateHash());

        algoPanel.add(new JLabel("Algorithm:"));
        algoPanel.add(algoBox);
        algoPanel.add(generateBtn);

        center.add(algoPanel);
        center.add(Box.createVerticalStrut(12));

        resultArea = new JTextArea(5, 40);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));

        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(
                BorderFactory.createTitledBorder("Result Hash")
        );

        center.add(resultScroll);
        center.add(Box.createVerticalStrut(12));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");

        center.add(progressBar);

        root.add(center, BorderLayout.CENTER);


        JPanel footer = new JPanel(new BorderLayout(10, 0));

        JButton copyBtn = new JButton("Copy");
        copyBtn.setPreferredSize(btnSize);
        copyBtn.addActionListener(e -> copyHash());

        JComboBox<Theme> themeBox = new JComboBox<>(Theme.values());
        themeBox.setPreferredSize(btnSize);
        themeBox.setSelectedItem(
                Theme.valueOf(PREFS.get(PREF_THEME, Theme.SYSTEM.name()))
        );
        themeBox.addActionListener(e ->
                switchTheme((Theme) themeBox.getSelectedItem())
        );

        JButton resetBtn = new JButton("Reset");
        resetBtn.setPreferredSize(btnSize);
        resetBtn.addActionListener(e -> resetAll());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.add(new JLabel("Theme:"));
        left.add(themeBox);
        left.add(copyBtn);
        left.add(resetBtn);

        JLabel footerLabel = new JLabel("© rugved.dev");
        footerLabel.setForeground(Color.GRAY);

        footer.add(left, BorderLayout.WEST);
        footer.add(footerLabel, BorderLayout.EAST);

        root.add(footer, BorderLayout.SOUTH);
    }


    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            fileField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void generateHash() {
        progressBar.setValue(0);
        progressBar.setString("Processing...");

        if (selectedFile == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a file first.",
                    "No File Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // resultArea.setText("");
        progressBar.setValue(0);
        progressBar.setString("Processing...");

        SwingWorker<String, Integer> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return HashService.generateHash(
                        selectedFile,
                        ((HashAlgorithm) Objects.requireNonNull(algoBox.getSelectedItem())).getAlgorithm(),
                        this::setProgress
                );
            }

            @Override
            protected void done() {
                try {
                    String hash = get();
                    String algo = algoBox.getSelectedItem().toString();
                    String fileName = selectedFile.getName();

                    resultArea.append(
                            "Algorithm : " + algo + "\n" +
                                    "File      : " + fileName + "\n" +
                                    "Hash      : " + hash + "\n" +
                                    "-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-\n"
                    );

                    progressBar.setString("Completed");

                } catch (Exception e) {
                    progressBar.setString("Failed");
                    JOptionPane.showMessageDialog(
                            FileHashGenerator.this,
                            e.getMessage(),
                            "Hash Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });

        worker.execute();
    }

    private void copyHash() {
        if (resultArea.getText().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Nothing to copy.",
                    "Copy",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(
                        new StringSelection(resultArea.getText()),
                        null
                );
    }

    private void switchTheme(Theme theme) {
        try {
            PREFS.put(PREF_THEME, theme.name());

            switch (theme) {
                case LIGHT -> FlatLightLaf.setup();
                case DARK -> FlatDarkLaf.setup();
                case DRACULA -> FlatDraculaIJTheme.setup();
                case SYSTEM -> {
                    if (isDark)
                        FlatDarkLaf.setup();
                    else
                        FlatLightLaf.setup();
                }
            }

            FlatLaf.updateUI();
            SwingUtilities.updateComponentTreeUI(this);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Theme switch failed",
                    "Theme",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void resetAll() {
        selectedFile = null;
        fileField.setText("");
        resultArea.setText("");
        progressBar.setValue(0);
        progressBar.setString("Ready");
            JOptionPane.showMessageDialog(
                    this,
                    "Data Reset Complete! Let's get you set up",
                    "Reset",
                    JOptionPane.INFORMATION_MESSAGE
            );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new FileHashGenerator().setVisible(true)
        );
    }
}
