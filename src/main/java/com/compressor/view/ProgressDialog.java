package com.compressor.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// Clase que representa un cuadro de diálogo para mostrar el progreso de la compresión
public class ProgressDialog extends JDialog {
    // Barras de progreso para el progreso total y el archivo actual
    private JProgressBar overallProgressBar;
    private JProgressBar currentFileProgressBar;
    
    // Etiquetas para mostrar información sobre el progreso
    private JLabel currentFileLabel;
    private JLabel overallProgressLabel;
    private JLabel timeRemainingLabel;
    private JLabel processedSizeLabel;
    
    // Botón para cancelar la operación
    private JButton cancelButton;
    
    // Variable para indicar si el usuario canceló la operación
    private boolean userCancelled;

    // Constructor que inicializa la interfaz
    public ProgressDialog(JFrame parent) {
        super(parent, "Compression Progress", true); // Título del cuadro de diálogo y modo modal
        initializeUI(); // Inicializa los componentes
        setupLayout();  // Organiza los componentes en el diseño
        configureDialog(); // Configura propiedades del cuadro de diálogo
    }

    // Método para inicializar los componentes de la interfaz
    private void initializeUI() {
        // Configuración de la barra de progreso general
        overallProgressBar = new JProgressBar(0, 100);
        overallProgressBar.setStringPainted(true); // Muestra el porcentaje
        overallProgressLabel = new JLabel("Overall progress: 0%"); // Texto inicial
        
        // Configuración de la barra de progreso del archivo actual
        currentFileProgressBar = new JProgressBar(0, 100);
        currentFileProgressBar.setStringPainted(true);
        currentFileLabel = new JLabel("Current file: ");
        currentFileLabel.setFont(currentFileLabel.getFont().deriveFont(Font.BOLD)); // Texto en negrita
        
        // Etiquetas informativas
        timeRemainingLabel = new JLabel("Time remaining: calculating...");
        processedSizeLabel = new JLabel("Processed: 0 MB of 0 MB");

        // Configuración del botón de cancelar
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            userCancelled = true; // Marca que el usuario canceló
            setVisible(false); // Oculta el cuadro de diálogo
        });

        // Agregar listener para detectar el cierre de la ventana
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                userCancelled = true; // Marca la cancelación si el usuario cierra la ventana
            }
        });
    }

    // Método para organizar los componentes en el diseño de la ventana
    private void setupLayout() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); // Diseño en columna
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Márgenes

        // Panel para mostrar el archivo actual y su progreso
        JPanel currentFilePanel = new JPanel(new BorderLayout(5, 5));
        currentFilePanel.add(currentFileLabel, BorderLayout.NORTH);
        currentFilePanel.add(currentFileProgressBar, BorderLayout.CENTER);
        
        // Panel para el progreso general
        JPanel overallPanel = new JPanel(new BorderLayout(5, 5));
        overallPanel.add(overallProgressLabel, BorderLayout.NORTH);
        overallPanel.add(overallProgressBar, BorderLayout.CENTER);
        
        // Panel para las etiquetas informativas
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        infoPanel.add(processedSizeLabel);
        infoPanel.add(timeRemainingLabel);
        
        // Agregar secciones al panel principal
        mainPanel.add(currentFilePanel);
        mainPanel.add(Box.createVerticalStrut(10)); // Espaciador
        mainPanel.add(overallPanel);
        mainPanel.add(Box.createVerticalStrut(10)); // Espaciador
        mainPanel.add(infoPanel);
        mainPanel.add(Box.createVerticalStrut(15)); // Espaciador
        mainPanel.add(cancelButton); // Botón de cancelar
        
        // Centrar el botón de cancelar
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        this.add(mainPanel); // Agregar el panel principal al cuadro de diálogo
    }

    // Configuración del cuadro de diálogo
    private void configureDialog() {
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // Evita cerrar con la "X"
        this.setSize(400, 300); // Tamaño fijo de la ventana
        this.setLocationRelativeTo(getParent()); // Centra la ventana respecto al padre
        this.setResizable(false); // No permite cambiar el tamaño
    }

    // Actualiza el progreso del archivo actual
    public void updateCurrentFile(String fileName, int progress) {
        SwingUtilities.invokeLater(() -> { // Asegura que se ejecute en el hilo de la interfaz gráfica
            currentFileLabel.setText("Current file: " + fileName);
            currentFileProgressBar.setValue(progress);
            currentFileProgressBar.setString(progress + "%");
        });
    }

    // Actualiza el progreso general de la compresión
    public void updateOverallProgress(int progress, String processedSize, 
                                    String totalSize, String timeRemaining) {
        SwingUtilities.invokeLater(() -> { // Asegura actualización en el hilo de la interfaz
            overallProgressBar.setValue(progress);
            overallProgressBar.setString(progress + "%");
            overallProgressLabel.setText("Overall progress: " + progress + "%");
            processedSizeLabel.setText("Processed: " + processedSize + " of " + totalSize);
            timeRemainingLabel.setText("Time remaining: " + timeRemaining);
        });
    }

    // Muestra el cuadro de diálogo
    public void showDialog() {
        userCancelled = false; // Reinicia el estado de cancelación
        SwingUtilities.invokeLater(() -> setVisible(true)); // Muestra la ventana en el hilo adecuado
    }

    // Verifica si el usuario ha cancelado la operación
    public boolean isCancelled() {
        return userCancelled;
    }

    // Muestra un mensaje cuando la compresión se completa
    public void showCompletion(boolean success) {
        SwingUtilities.invokeLater(() -> {
            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "Compression completed successfully!", 
                    "Completed", 
                    JOptionPane.INFORMATION_MESSAGE); // Mensaje de éxito
            }
            setVisible(false); // Cierra el cuadro de diálogo
        });
    }
}