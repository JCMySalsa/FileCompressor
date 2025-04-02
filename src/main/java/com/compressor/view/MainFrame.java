package com.compressor.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class MainFrame extends JFrame {
    private JButton selectFilesButton;
    private JButton compressButton;
    private JButton cancelButton;
    private JList<String> filesList;
    private DefaultListModel<String> listModel;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private JLabel fileCountLabel;
    private JLabel totalSizeLabel;

    public MainFrame() {
        super("File Compressor");
        initializeComponents();
        setupLayout();
        configureWindow();
    }

    private void initializeComponents() {
        // Buttons without icons
        selectFilesButton = new JButton("Select Files");
        compressButton = new JButton("Compress");
        compressButton.setEnabled(false);
        cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(false);

        // File list
        listModel = new DefaultListModel<>();
        filesList = new JList<>(listModel);
        filesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        filesList.setVisibleRowCount(10);

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);

        // Info labels
        statusLabel = new JLabel("Select files to compress");
        fileCountLabel = new JLabel("0 files selected");
        totalSizeLabel = new JLabel("Total size: 0 KB");
    }

    private void setupLayout() {
        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Button panel at top
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(selectFilesButton);
        buttonPanel.add(compressButton);
        buttonPanel.add(cancelButton);

        // File list with scroll
        JScrollPane scrollPane = new JScrollPane(filesList);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        // Info panel at bottom
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        infoPanel.add(fileCountLabel);
        infoPanel.add(totalSizeLabel);

        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        statusPanel.add(progressBar, BorderLayout.SOUTH);

        // Assemble main panel
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);

        // Add to frame
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null); // Center window
    }

    // Public methods to update UI
    public void updateFileList(String[] fileNames) {
        listModel.clear();
        for(String fileName : fileNames) {
            listModel.addElement(fileName);
        }
        filesList.repaint();
    }

    public void updateFileInfo(int count, String totalSize) {
        fileCountLabel.setText(count + " files selected");
        totalSizeLabel.setText("Total size: " + totalSize);
        compressButton.setEnabled(count > 0);
    }

    public void showProgress(boolean show) {
        progressBar.setVisible(show);
        progressBar.setValue(0);
        selectFilesButton.setEnabled(!show);
        compressButton.setEnabled(!show && listModel.size() > 0);
        cancelButton.setEnabled(show);
    }

    public void updateProgress(int progress) {
        progressBar.setValue(progress);
        statusLabel.setText("Compressing... " + progress + "% complete");
    }

    public void showCompletion(boolean success) {
        progressBar.setVisible(false);
        selectFilesButton.setEnabled(true);
        compressButton.setEnabled(true);
        cancelButton.setEnabled(false);
        
        if (success) {
            statusLabel.setText("Compression completed successfully!");
            JOptionPane.showMessageDialog(this, 
                "Files were compressed successfully", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, 
            message, 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        statusLabel.setText("Error: " + message);
    }

    // Listener methods
    public void addSelectFilesListener(ActionListener listener) {
        selectFilesButton.addActionListener(listener);
    }

    public void addCompressListener(ActionListener listener) {
        compressButton.addActionListener(listener);
    }

    public void addCancelListener(ActionListener listener) {
        cancelButton.addActionListener(listener);
    }
}