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
        super(parent, "Progreso de Compresión", true);
        initializeUI();
        setupLayout();
        configureDialog();
    }

    private void initializeUI() {
        overallProgressBar = new JProgressBar(0, 100);
        overallProgressBar.setStringPainted(true);
        
        currentFileProgressBar = new JProgressBar(0, 100);
        currentFileProgressBar.setStringPainted(true);
        
        currentFileLabel = new JLabel("Archivo actual: ");
        currentFileLabel.setFont(currentFileLabel.getFont().deriveFont(Font.BOLD));
        
        overallProgressLabel = new JLabel("Progreso general: 0%");
        timeRemainingLabel = new JLabel("Tiempo estimado: --");
        processedSizeLabel = new JLabel("Procesados: 0 MB de 0 MB");

        cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> {
            userCancelled = true;
            setVisible(false);
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                userCancelled = true;
            }
        });
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(currentFileLabel, gbc);

        gbc.gridy = 1;
        mainPanel.add(currentFileProgressBar, gbc);

        gbc.gridy = 2;
        mainPanel.add(new JSeparator(), gbc);

        gbc.gridy = 3;
        mainPanel.add(overallProgressLabel, gbc);

        gbc.gridy = 4;
        mainPanel.add(overallProgressBar, gbc);

        gbc.gridy = 5;
        mainPanel.add(processedSizeLabel, gbc);

        gbc.gridy = 6;
        mainPanel.add(timeRemainingLabel, gbc);

        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(cancelButton, gbc);

        this.add(mainPanel);
    }

    private void configureDialog() {
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setSize(500, 350);
        this.setLocationRelativeTo(getParent());
        this.setResizable(false);
    }

    public void updateCurrentFile(String fileName, int progress) {
        currentFileLabel.setText("Archivo actual: " + fileName);
        currentFileProgressBar.setValue(progress);
        currentFileProgressBar.setString(progress + "%");
    }

    public void updateOverallProgress(int progress, String processedSize, 
                                    String totalSize, String timeRemaining) {
        overallProgressBar.setValue(progress);
        overallProgressBar.setString(progress + "%");
        overallProgressLabel.setText("Progreso general: " + progress + "%");
        processedSizeLabel.setText("Procesados: " + processedSize + " de " + totalSize);
        timeRemainingLabel.setText("Tiempo estimado: " + timeRemaining);
    }

    public void showDialog() {
        userCancelled = false;
        setVisible(true);
    }

    public boolean isCancelled() {
        return userCancelled;
    }

    public void showCompletion(boolean success) {
        if (success) {
            JOptionPane.showMessageDialog(this, 
                "Compresión completada con éxito!", 
                "Completado", 
                JOptionPane.INFORMATION_MESSAGE);
        }
        setVisible(false);
    }
}