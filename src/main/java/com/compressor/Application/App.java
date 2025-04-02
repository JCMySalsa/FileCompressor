package com.compressor.Application;

import javax.swing.SwingUtilities;

import com.compressor.controller.FileCompressionController;
import com.compressor.model.FileCompressor;
import com.compressor.model.FileSelectionModel;
import com.compressor.model.ProgressData;
import com.compressor.view.MainFrame;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Inicializa los modelos
            FileSelectionModel selectionModel = new FileSelectionModel();
            FileCompressor compressor = new FileCompressor();
            ProgressData progressData = new ProgressData();
            
            // Inicializa view
            MainFrame mainFrame = new MainFrame();
            
            // Inicializa controller
            FileCompressionController controller = new FileCompressionController(
                selectionModel, compressor, progressData, mainFrame);
            
            // Muestra el main frame
            mainFrame.setVisible(true);
        });
    }
}