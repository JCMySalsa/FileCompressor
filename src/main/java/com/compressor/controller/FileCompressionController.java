package com.compressor.controller; // Define el paquete donde está este archivo

import com.compressor.model.*; // Importa los modelos necesarios
import com.compressor.view.*; // Importa las vistas necesarias
import javax.swing.*; // Importa librerías de Swing para la interfaz gráfica
import java.io.File; // Importa la clase File para manejar archivos
import java.util.List; // Importa la clase List para manejar listas de archivos
import java.util.concurrent.ExecutorService; // Importa ExecutorService para manejar hilos
import java.util.concurrent.Executors; // Importa Executors para crear un solo hilo

public class FileCompressionController { // Controlador principal de la compresión de archivos
    private final FileSelectionModel selectionModel; // Modelo de selección de archivos
    private final FileCompressor compressor; // Modelo de compresión de archivos
    private final MainFrame mainView; // Vista principal
    private final ProgressDialog progressDialog; // Diálogo de progreso
    private final ExecutorService executor; // Manejador de hilos
    private final ProgressData progressData; // Datos de progreso de la compresión

    public FileCompressionController(FileSelectionModel selectionModel, FileCompressor compressor, MainFrame mainView) {
        this.selectionModel = selectionModel;
        this.compressor = compressor;
        this.mainView = mainView;
        this.progressData = new ProgressData(); // Inicializa el modelo de progreso
        this.progressDialog = new ProgressDialog(mainView); // Crea el diálogo de progreso
        this.executor = Executors.newSingleThreadExecutor(); // Crea un solo hilo para la tarea
        
        setupEventHandlers(); // Configura los manejadores de eventos
    }

    private void setupEventHandlers() { // Configura los eventos de los botones
        mainView.addSelectFilesListener(e -> handleFileSelection()); // Maneja la selección de archivos
        mainView.addCompressListener(e -> handleCompression()); // Maneja la compresión
        mainView.addCancelListener(e -> handleCancellation()); // Maneja la cancelación
    }

    private void handleFileSelection() { // Maneja la selección de archivos
        if (selectionModel.showFileSelectionDialog()) { // Si el usuario selecciona archivos
            mainView.updateFileList(selectionModel.getSelectedFileNames()); // Actualiza la lista en la interfaz
            mainView.updateFileInfo(
                selectionModel.getFileCount(),
                selectionModel.getFormattedTotalSize()
            ); // Muestra el tamaño total
        }
    }

    private void handleCompression() { // Maneja la compresión
        List<File> files = selectionModel.getSelectedFiles(); // Obtiene los archivos seleccionados
        if (files.isEmpty()) { // Si no hay archivos seleccionados, muestra un error
            mainView.showError("No files selected");
            return;
        }

        JFileChooser fileChooser = new JFileChooser(); // Crea un cuadro de diálogo para guardar el ZIP
        fileChooser.setDialogTitle("Save ZIP File");
        fileChooser.setSelectedFile(new File("compressed.zip"));
        
        if (fileChooser.showSaveDialog(mainView) == JFileChooser.APPROVE_OPTION) { // Si el usuario selecciona guardar
            File outputFile = fileChooser.getSelectedFile();
            String outputPath = outputFile.getAbsolutePath();
            
            if (!outputPath.toLowerCase().endsWith(".zip")) { // Si no termina en .zip, lo agrega
                outputPath += ".zip";
            }
            
            prepareAndStartCompression(files, outputPath); // Prepara y comienza la compresión
        }
    }

    private void prepareAndStartCompression(List<File> files, String outputPath) { // Configura y empieza la compresión
        progressData.initialize(files.size(), selectionModel.getTotalSize()); // Inicializa los datos de progreso
        compressor.setFilesToCompress(files); // Establece los archivos a comprimir
        compressor.setOutputPath(outputPath); // Establece la ruta de salida

        compressor.setCompressionListener(new FileCompressor.CompressionListener() {
            @Override
            public void onProgressUpdate(int fileIndex, int progress) { // Maneja el progreso de compresión
                File currentFile = files.get(fileIndex);
                progressData.updateCurrentFileProgress(
                    currentFile.getName(),
                    currentFile.length(),
                    (long) (currentFile.length() * progress / 100.0)
                );
                
                SwingUtilities.invokeLater(() -> {
                    progressDialog.updateCurrentFile(currentFile.getName(), progress);
                    progressDialog.updateOverallProgress(
                        progressData.getOverallProgress(),
                        progressData.getProcessedSize(),
                        progressData.getTotalSize(),
                        progressData.getFormattedRemainingTime()
                    );
                });
            }

            @Override
            public void onFileComplete(int fileIndex) { // Cuando un archivo se completa
                progressData.completeCurrentFile();
            }

            @Override
            public void onCompressionComplete() { // Cuando la compresión termina
                SwingUtilities.invokeLater(() -> {
                    progressDialog.showCompletion(true);
                    mainView.showCompletion(true);
                    executor.shutdown();
                });
            }

            @Override
            public void onError(File file, Exception e) { // Maneja errores durante la compresión
                SwingUtilities.invokeLater(() -> {
                    progressDialog.showCompletion(false);
                    mainView.showError("Error compressing " + (file != null ? file.getName() : "") + ": " + e.getMessage());
                    executor.shutdown();
                });
            }
        });

        mainView.showProgress(true); // Muestra el progreso en la interfaz
        progressDialog.showDialog(); // Muestra el cuadro de progreso
        
        executor.execute(() -> { // Ejecuta la compresión en un hilo separado
            boolean success = compressor.startCompression();
            SwingUtilities.invokeLater(() -> {
                mainView.showProgress(false);
                if (!success && !progressDialog.isCancelled()) {
                    mainView.showError("Compression failed - some files may not have been compressed");
                }
            });
        });
    }

    private void handleCancellation() { // Maneja la cancelación de la compresión
        executor.shutdownNow(); // Detiene el proceso de compresión
        progressDialog.setVisible(false); // Oculta el cuadro de diálogo
        mainView.showProgress(false); // Oculta el indicador de progreso
        mainView.showError("Compression cancelled by user"); // Muestra un mensaje de cancelación
    }
}
