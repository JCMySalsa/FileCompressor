package com.compressor.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class MainFrame extends JFrame {
    // Botones principales de la interfaz
    private JButton selectFilesButton;
    private JButton compressButton;
    private JButton cancelButton;

    // Lista de archivos seleccionados
    private JList<String> filesList;
    private DefaultListModel<String> listModel;

    // Etiquetas y barra de progreso
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private JLabel fileCountLabel;
    private JLabel totalSizeLabel;

    public MainFrame() {
        super("File Compressor"); // Título de la ventana principal
        initializeComponents(); // Inicializa los componentes gráficos
        setupLayout(); // Configura el diseño de los componentes
        configureWindow(); // Configura propiedades de la ventana
    }

    private void initializeComponents() {
        // Botones de acción
        selectFilesButton = new JButton("Select Files");
        compressButton = new JButton("Compress");
        compressButton.setEnabled(false); // Deshabilitado inicialmente
        cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(false); // Deshabilitado inicialmente

        // Lista de archivos con modelo para actualizar dinámicamente
        listModel = new DefaultListModel<>();
        filesList = new JList<>(listModel);
        filesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        filesList.setVisibleRowCount(10);

        // Barra de progreso
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true); // Muestra el porcentaje de progreso
        progressBar.setVisible(false); // Oculta inicialmente

        // Etiquetas de información
        statusLabel = new JLabel("Select files to compress");
        fileCountLabel = new JLabel("0 files selected");
        totalSizeLabel = new JLabel("Total size: 0 KB");
    }

    private void setupLayout() {
        // Panel principal con espaciado
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de botones en la parte superior
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(selectFilesButton);
        buttonPanel.add(compressButton);
        buttonPanel.add(cancelButton);

        // Panel con lista de archivos y scroll
        JScrollPane scrollPane = new JScrollPane(filesList);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        // Panel de información en la parte inferior
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        infoPanel.add(fileCountLabel);
        infoPanel.add(totalSizeLabel);

        // Panel de estado y barra de progreso
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        statusPanel.add(progressBar, BorderLayout.SOUTH);

        // Ensamblar los paneles en el panel principal
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);

        // Agregar el panel principal y de estado a la ventana
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Cierra la aplicación al cerrar la ventana
        setSize(600, 500); // Tamaño de la ventana
        setLocationRelativeTo(null); // Centrar ventana en la pantalla
    }

    // Métodos para actualizar la interfaz gráfica
    public void updateFileList(String[] fileNames) {
        listModel.clear(); // Borra la lista actual
        for (String fileName : fileNames) {
            listModel.addElement(fileName); // Agrega los nuevos archivos
        }
        filesList.repaint(); // Refresca la lista
    }

    public void updateFileInfo(int count, String totalSize) {
        fileCountLabel.setText(count + " files selected"); // Actualiza el contador de archivos
        totalSizeLabel.setText("Total size: " + totalSize); // Actualiza el tamaño total
        compressButton.setEnabled(count > 0); // Habilita/deshabilita el botón de compresión
    }

    public void showProgress(boolean show) {
        progressBar.setVisible(show); // Muestra u oculta la barra de progreso
        progressBar.setValue(0);
        selectFilesButton.setEnabled(!show);
        compressButton.setEnabled(!show && listModel.size() > 0);
        cancelButton.setEnabled(show);
    }

    public void updateProgress(int progress) {
        progressBar.setValue(progress); // Actualiza el valor de la barra de progreso
        statusLabel.setText("Compressing... " + progress + "% complete"); // Muestra el progreso en texto
    }

    public void showCompletion(boolean success) {
        progressBar.setVisible(false);
        selectFilesButton.setEnabled(true);
        compressButton.setEnabled(true);
        cancelButton.setEnabled(false);
        if (success) {
            statusLabel.setText("Compression completed successfully!"); // Mensaje de éxito
            JOptionPane.showMessageDialog(this, "Files were compressed successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        statusLabel.setText("Error: " + message); // Mensaje de error
    }

    // Métodos para agregar listeners a los botones
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
