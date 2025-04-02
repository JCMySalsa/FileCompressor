package com.compressor.Application;
//hello world

import javax.swing.SwingUtilities;

import com.compressor.controller.FileCompressionController;
import com.compressor.model.FileCompressor;
import com.compressor.model.FileSelectionModel;
import com.compressor.model.ProgressData;
import com.compressor.view.MainFrame;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Inicializa modelo

            // Inicializa los modelos
            FileSelectionModel selectionModel = new FileSelectionModel();
            FileCompressor compressor = new FileCompressor();
            ProgressData progressData = new ProgressData();
            
            // Inicializa view
            MainFrame mainFrame = new MainFrame();
            
            // Inicializa controller
            FileCompressionController controller = new FileCompressionController(
                selectionModel, compressor, mainFrame);
            
            // Muestra el main frame
            mainFrame.setVisible(true);
        });
    }
}