package com.compressor.controller;

import com.compressor.model.*;
import com.compressor.view.*;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

public class FileCompressionController {
    private FileSelectionModel selectionModel;
    private FileCompressor compressor;
    private ProgressData progressData;
    private MainFrame mainView;
    private ProgressDialog progressDialog;
    private ExecutorService executor;
    
    public FileCompressionController(FileSelectionModel selectionModel, 
                                   FileCompressor compressor,
                                   ProgressData progressData,
                                   MainFrame mainView) {
        this.selectionModel = selectionModel;
        this.compressor = compressor;
        this.progressData = progressData;
        this.mainView = mainView;
        this.progressDialog = new ProgressDialog(mainView);
        this.executor = Executors.newSingleThreadExecutor();
        
        setupEventHandlers();
    }
    
    private void setupEventHandlers() {
        mainView.addSelectFilesListener(e -> handleFileSelection());
        mainView.addCompressListener(e -> handleCompression());
        mainView.addCancelListener(e -> handleCancellation());
    }
    
    private void handleFileSelection() {
        if (selectionModel.showFileSelectionDialog()) {
            mainView.updateFileList(selectionModel.getSelectedFileNames());
            mainView.updateFileInfo(
                selectionModel.getFileCount(), 
                selectionModel.getFormattedTotalSize());
        }
    }
    
    private void handleCompression() {
        List<File> files = selectionModel.getSelectedFiles();
        if (files.isEmpty()) {
            mainView.showError("No hay archivos seleccionados");
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar archivo ZIP");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        if (fileChooser.showSaveDialog(mainView) == JFileChooser.APPROVE_OPTION) {
            String outputPath = fileChooser.getSelectedFile().getAbsolutePath() + "/compressed.zip";
            
            progressData.initialize(files.size(), selectionModel.getTotalSize());
            compressor.setFilesToCompress(files);
            compressor.setOutputPath(outputPath);
            compressor.setCompressionListener(new FileCompressor.CompressionListener() {
                @Override
                public void onProgressUpdate(int fileIndex, int progress) {
                    File file = files.get(fileIndex);
                    progressData.updateCurrentFileProgress(
                        file.getName(), file.length(), 
                        (long) (file.length() * progress / 100.0));
                    
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.updateCurrentFile(
                            file.getName(), progress);
                        progressDialog.updateOverallProgress(
                            progressData.getOverallProgress(),
                            progressData.getProcessedSize(),
                            progressData.getTotalSize(),
                            progressData.getFormattedRemainingTime());
                    });
                }
                
                @Override
                public void onFileComplete(int fileIndex) {
                    progressData.completeCurrentFile();
                }
                
                @Override
                public void onCompressionComplete() {
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.showCompletion(true);
                        mainView.showCompletion(true);
                    });
                }
                
                @Override
                public void onError(File file, Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.showCompletion(false);
                        mainView.showError("Error al comprimir: " + e.getMessage());
                    });
                }
            });
            
            mainView.showProgress(true);
            progressDialog.showDialog();
            
            executor.execute(() -> {
                boolean success = compressor.startCompression();
                SwingUtilities.invokeLater(() -> {
                    mainView.showProgress(false);
                    if (!success && !progressDialog.isCancelled()) {
                        mainView.showError("Error durante la compresión");
                    }
                });
            });
        }
    }
    
    private void handleCancellation() {
        compressor.cancelCompression();
        progressDialog.setVisible(false);
        mainView.showProgress(false);
        mainView.showError("Compresión cancelada por el usuario");
    }
}