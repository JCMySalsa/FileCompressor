package com.compressor.controller;

import com.compressor.model.*;
import com.compressor.view.*;
import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileCompressionController {
    private final FileSelectionModel selectionModel;
    private final FileCompressor compressor;
    private final MainFrame mainView;
    private final ProgressDialog progressDialog;
    private final ExecutorService executor;
    private final ProgressData progressData;

    public FileCompressionController(FileSelectionModel selectionModel,
                                   FileCompressor compressor,
                                   MainFrame mainView) {
        this.selectionModel = selectionModel;
        this.compressor = compressor;
        this.mainView = mainView;
        this.progressData = new ProgressData();
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
            mainView.showError("No files selected");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save ZIP File");
        fileChooser.setSelectedFile(new File("compressed.zip"));
        
        if (fileChooser.showSaveDialog(mainView) == JFileChooser.APPROVE_OPTION) {
            File outputFile = fileChooser.getSelectedFile();
            String outputPath = outputFile.getAbsolutePath();
            
            if (!outputPath.toLowerCase().endsWith(".zip")) {
                outputPath += ".zip";
            }

            prepareAndStartCompression(files, outputPath);
        }
    }

    private void prepareAndStartCompression(List<File> files, String outputPath) {
        progressData.initialize(files.size(), selectionModel.getTotalSize());
        
        compressor.setFilesToCompress(files);
        compressor.setOutputPath(outputPath);
        
        compressor.setCompressionListener(new FileCompressor.CompressionListener() {
            @Override
            public void onProgressUpdate(int fileIndex, int progress) {
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
            public void onFileComplete(int fileIndex) {
                progressData.completeCurrentFile();
            }
            
            @Override
            public void onCompressionComplete() {
                SwingUtilities.invokeLater(() -> {
                    progressDialog.showCompletion(true);
                    mainView.showCompletion(true);
                    executor.shutdown();
                });
            }
            
            @Override
            public void onError(File file, Exception e) {
                SwingUtilities.invokeLater(() -> {
                    progressDialog.showCompletion(false);
                    mainView.showError("Error compressing " + (file != null ? file.getName() : "") + 
                                     ": " + e.getMessage());
                    executor.shutdown();
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
                    mainView.showError("Compression failed - some files may not have been compressed");
                }
            });
        });
    }

    private void handleCancellation() {
        executor.shutdownNow();
        progressDialog.setVisible(false);
        mainView.showProgress(false);
        mainView.showError("Compression cancelled by user");
    }
}