package com.compressor.controller; // Define el paquete donde está este archivo

import com.compressor.model.*; // Importa los modelos necesarios para la lógica de la compresión de archivos
import com.compressor.view.*; // Importa las vistas necesarias para la interfaz gráfica
import javax.swing.*; // Importa las librerías de Swing para la interfaz gráfica
import java.io.File; // Importa la clase File para trabajar con archivos del sistema
import java.util.List; // Importa la clase List para manejar las listas de archivos seleccionados
import java.util.concurrent.ExecutorService; // Importa ExecutorService para manejar la ejecución de hilos
import java.util.concurrent.Executors; // Importa Executors para crear un solo hilo para la tarea

public class FileCompressionController { // Controlador principal de la compresión de archivos
    private final FileSelectionModel selectionModel; // Instancia del modelo que maneja la selección de archivos
    private final FileCompressor compressor; // Instancia del modelo que maneja la compresión de archivos
    private final MainFrame mainView; // Vista principal de la aplicación
    private final ProgressDialog progressDialog; // Diálogo de progreso que muestra el estado de la compresión
    private final ExecutorService executor; // ExecutorService que maneja la ejecución de tareas en hilos
    private final ProgressData progressData; // Contenedor para los datos de progreso de la compresión de archivos

    // Constructor que recibe los modelos y la vista para inicializar el controlador
    public FileCompressionController(FileSelectionModel selectionModel, FileCompressor compressor, MainFrame mainView) {
        this.selectionModel = selectionModel; // Inicializa el modelo de selección de archivos
        this.compressor = compressor; // Inicializa el modelo de compresión de archivos
        this.mainView = mainView; // Inicializa la vista principal
        this.progressData = new ProgressData(); // Crea el modelo para el progreso de la compresión
        this.progressDialog = new ProgressDialog(mainView); // Crea el cuadro de diálogo para mostrar el progreso
        this.executor = Executors.newSingleThreadExecutor(); // Crea un solo hilo para gestionar las tareas

        setupEventHandlers(); // Configura los manejadores de eventos para los botones de la vista
    }

    // Configura los eventos de la interfaz gráfica
    private void setupEventHandlers() {
        mainView.addSelectFilesListener(e -> handleFileSelection()); // Maneja la selección de archivos
        mainView.addCompressListener(e -> handleCompression()); // Maneja el evento de compresión
        mainView.addCancelListener(e -> handleCancellation()); // Maneja la cancelación de la compresión
    }

    // Método que maneja la selección de archivos por parte del usuario
    private void handleFileSelection() {
        if (selectionModel.showFileSelectionDialog()) { // Si el usuario selecciona archivos
            mainView.updateFileList(selectionModel.getSelectedFileNames()); // Actualiza la lista de archivos seleccionados en la vista
            mainView.updateFileInfo(
                selectionModel.getFileCount(),
                selectionModel.getFormattedTotalSize()
            ); // Muestra la cantidad total de archivos y su tamaño en la interfaz
        }
    }

    // Método que maneja la compresión de archivos
    private void handleCompression() {
        List<File> files = selectionModel.getSelectedFiles(); // Obtiene la lista de archivos seleccionados
        if (files.isEmpty()) { // Si no se han seleccionado archivos
            mainView.showError("No files selected"); // Muestra un mensaje de error
            return;
        }

        JFileChooser fileChooser = new JFileChooser(); // Crea un cuadro de diálogo para guardar el archivo comprimido
        fileChooser.setDialogTitle("Save ZIP File"); // Título del cuadro de diálogo
        fileChooser.setSelectedFile(new File("compressed.zip")); // Establece un archivo por defecto para guardar

        // Si el usuario elige un archivo para guardar
        if (fileChooser.showSaveDialog(mainView) == JFileChooser.APPROVE_OPTION) {
            File outputFile = fileChooser.getSelectedFile(); // Obtiene el archivo seleccionado para guardar
            String outputPath = outputFile.getAbsolutePath(); // Obtiene la ruta absoluta del archivo de salida

            // Si el archivo no tiene la extensión .zip, la agrega
            if (!outputPath.toLowerCase().endsWith(".zip")) {
                outputPath += ".zip";
            }

            // Prepara y comienza la compresión de los archivos seleccionados
            prepareAndStartCompression(files, outputPath);
        }
    }

    // Configura y comienza la compresión de los archivos seleccionados
    private void prepareAndStartCompression(List<File> files, String outputPath) {
        progressData.initialize(files.size(), selectionModel.getTotalSize()); // Inicializa los datos de progreso de la compresión
        compressor.setFilesToCompress(files); // Establece los archivos a comprimir en el compresor
        compressor.setOutputPath(outputPath); // Establece la ruta de salida para el archivo comprimido

        // Establece el listener que maneja los eventos de progreso
        compressor.setCompressionListener(new FileCompressor.CompressionListener() {
            @Override
            public void onProgressUpdate(int fileIndex, int progress) { // Actualiza el progreso de la compresión
                File currentFile = files.get(fileIndex); // Obtiene el archivo actual
                progressData.updateCurrentFileProgress(
                    currentFile.getName(), // Nombre del archivo
                    currentFile.length(), // Tamaño total del archivo
                    (long) (currentFile.length() * progress / 100.0) // Tamaño procesado hasta el momento
                );

                SwingUtilities.invokeLater(() -> { // Actualiza la interfaz gráfica en el hilo principal
                    progressDialog.updateCurrentFile(currentFile.getName(), progress); // Actualiza el progreso del archivo actual
                    progressDialog.updateOverallProgress(
                        progressData.getOverallProgress(), // Progreso general de la compresión
                        progressData.getProcessedSize(), // Tamaño procesado hasta ahora
                        progressData.getTotalSize(), // Tamaño total de todos los archivos
                        progressData.getFormattedRemainingTime() // Tiempo restante formateado
                    );
                });
            }

            @Override
            public void onFileComplete(int fileIndex) { // Cuando un archivo ha sido comprimido
                progressData.completeCurrentFile(); // Marca el archivo como completado
            }

            @Override
            public void onCompressionComplete() { // Cuando la compresión de todos los archivos ha terminado
                SwingUtilities.invokeLater(() -> { // Actualiza la interfaz gráfica en el hilo principal
                    progressDialog.showCompletion(true); // Muestra un mensaje de finalización
                    mainView.showCompletion(true); // Informa que la compresión se completó
                    executor.shutdown(); // Detiene el executor después de la compresión
                });
            }

            @Override
            public void onError(File file, Exception e) { // Si ocurre un error durante la compresión
                SwingUtilities.invokeLater(() -> { // Actualiza la interfaz gráfica en el hilo principal
                    progressDialog.showCompletion(false); // Muestra que hubo un error
                    mainView.showError("Error compressing " + (file != null ? file.getName() : "") + ": " + e.getMessage()); // Muestra el mensaje de error
                    executor.shutdown(); // Detiene el executor
                });
            }
        });

        mainView.showProgress(true); // Muestra el indicador de progreso en la interfaz
        progressDialog.showDialog(); // Muestra el cuadro de diálogo de progreso

        // Ejecuta la tarea de compresión en un hilo separado
        executor.execute(() -> {
            boolean success = compressor.startCompression(); // Inicia la compresión de archivos
            SwingUtilities.invokeLater(() -> { // Actualiza la interfaz gráfica en el hilo principal
                mainView.showProgress(false); // Oculta el indicador de progreso
                if (!success && !progressDialog.isCancelled()) { // Si la compresión falla y no fue cancelada
                    mainView.showError("Compression failed - some files may not have been compressed"); // Muestra un mensaje de error
                }
            });
        });
    }

    // Maneja la cancelación de la compresión por parte del usuario
    private void handleCancellation() {
        executor.shutdownNow(); // Detiene inmediatamente el proceso de compresión
        progressDialog.setVisible(false); // Oculta el cuadro de diálogo de progreso
        mainView.showProgress(false); // Oculta el indicador de progreso
        mainView.showError("Compression cancelled by user"); // Muestra un mensaje informando que la compresión fue cancelada
    }
}

