package com.compressor.Application; // Define el paquete donde está este archivo

import javax.swing.SwingUtilities; // Importa SwingUtilities para manejar la interfaz gráfica
import com.compressor.controller.FileCompressionController; // Importa el controlador
import com.compressor.model.FileCompressor; // Importa la clase que comprime archivos
import com.compressor.model.FileSelectionModel; // Importa la clase que maneja la selección de archivos
import com.compressor.model.ProgressData; // Importa la clase que guarda el progreso de la compresión
import com.compressor.view.MainFrame; // Importa la ventana principal del programa

public class App { // Clase principal del programa
    public static void main(String[] args) { // Método principal donde comienza la ejecución
        SwingUtilities.invokeLater(() -> { // Ejecuta el código en un hilo separado para manejar la interfaz gráfica
            
            // Crea los modelos (componentes del programa)
            FileSelectionModel selectionModel = new FileSelectionModel(); // Modelo para seleccionar archivos
            FileCompressor compressor = new FileCompressor(); // Modelo que realiza la compresión
            ProgressData progressData = new ProgressData(); // Modelo que guarda el progreso de la compresión
            
            // Crea la ventana principal
            MainFrame mainFrame = new MainFrame();
            
            // Crea el controlador que conecta los modelos con la interfaz gráfica
            FileCompressionController controller = new FileCompressionController(selectionModel, compressor, mainFrame);
            
            // Muestra la ventana del programa
            mainFrame.setVisible(true);
        });
    }
}
