package com.compressor.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ProgressDialog extends JDialog {
    private JProgressBar overallProgressBar;
    private JProgressBar currentFileProgressBar;
    private JLabel currentFileLabel;
    private JLabel overallProgressLabel;
    private JLabel timeRemainingLabel;
    private JLabel processedSizeLabel;
    private JButton cancelButton;
    private boolean userCancelled;

    public ProgressDialog(JFrame parent) {
        super(parent, "Compression Progress", true);
        initializeUI();
        setupLayout();
        configureDialog();
    }

    private void initializeUI() {
        // Overall progress components
        overallProgressBar = new JProgressBar(0, 100);
        overallProgressBar.setStringPainted(true);
        overallProgressLabel = new JLabel("Overall progress: 0%");
        
        // Current file progress components
        currentFileProgressBar = new JProgressBar(0, 100);
        currentFileProgressBar.setStringPainted(true);
        currentFileLabel = new JLabel("Current file: ");
        currentFileLabel.setFont(currentFileLabel.getFont().deriveFont(Font.BOLD));
        
        // Information labels
        timeRemainingLabel = new JLabel("Time remaining: calculating...");
        processedSizeLabel = new JLabel("Processed: 0 MB of 0 MB");

        // Cancel button
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            userCancelled = true;
            setVisible(false);
        });

        // Window listener to handle close button
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                userCancelled = true;
            }
        });
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Current file section
        JPanel currentFilePanel = new JPanel(new BorderLayout(5, 5));
        currentFilePanel.add(currentFileLabel, BorderLayout.NORTH);
        currentFilePanel.add(currentFileProgressBar, BorderLayout.CENTER);
        
        // Overall progress section
        JPanel overallPanel = new JPanel(new BorderLayout(5, 5));
        overallPanel.add(overallProgressLabel, BorderLayout.NORTH);
        overallPanel.add(overallProgressBar, BorderLayout.CENTER);
        
        // Info section
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        infoPanel.add(processedSizeLabel);
        infoPanel.add(timeRemainingLabel);
        
        // Add sections to main panel
        mainPanel.add(currentFilePanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(overallPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(infoPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(cancelButton);

        // Center the cancel button
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        this.add(mainPanel);
    }

    private void configureDialog() {
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setSize(400, 300);
        this.setLocationRelativeTo(getParent());
        this.setResizable(false);
    }

    public void updateCurrentFile(String fileName, int progress) {
        SwingUtilities.invokeLater(() -> {
            currentFileLabel.setText("Current file: " + fileName);
            currentFileProgressBar.setValue(progress);
            currentFileProgressBar.setString(progress + "%");
        });
    }

    public void updateOverallProgress(int progress, String processedSize, 
                                    String totalSize, String timeRemaining) {
        SwingUtilities.invokeLater(() -> {
            overallProgressBar.setValue(progress);
            overallProgressBar.setString(progress + "%");
            overallProgressLabel.setText("Overall progress: " + progress + "%");
            processedSizeLabel.setText("Processed: " + processedSize + " of " + totalSize);
            timeRemainingLabel.setText("Time remaining: " + timeRemaining);
        });
    }

    public void showDialog() {
        userCancelled = false;
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
        });
    }

    public boolean isCancelled() {
        return userCancelled;
    }

    public void showCompletion(boolean success) {
        SwingUtilities.invokeLater(() -> {
            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "Compression completed successfully!", 
                    "Completed", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            setVisible(false);
        });
    }
}