package com.compressor.model;

/**
 * Modelo para almacenar y gestionar datos de progreso durante la compresión
 */
public class ProgressData {
    private int totalFiles;
    private int processedFiles;
    private int currentFileProgress;
    private String currentFileName;
    private long totalBytes;
    private long processedBytes;
    private long currentFileSize;
    private String currentStatus;
    private long startTime;
    private long estimatedRemainingTime;

    public ProgressData() {
        reset();
    }

    /**
     * Reinicia todos los datos de progreso
     */
    public void reset() {
        this.totalFiles = 0;
        this.processedFiles = 0;
        this.currentFileProgress = 0;
        this.currentFileName = "";
        this.totalBytes = 0;
        this.processedBytes = 0;
        this.currentFileSize = 0;
        this.currentStatus = "Listo";
        this.startTime = 0;
        this.estimatedRemainingTime = 0;
    }

    /**
     * Inicializa los datos para una nueva operación de compresión
     * @param totalFiles Número total de archivos a procesar
     * @param totalBytes Tamaño total en bytes de todos los archivos
     */
    public void initialize(int totalFiles, long totalBytes) {
        this.totalFiles = totalFiles;
        this.totalBytes = totalBytes;
        this.processedFiles = 0;
        this.processedBytes = 0;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Actualiza el progreso del archivo actual
     * @param fileName Nombre del archivo en procesamiento
     * @param fileSize Tamaño del archivo en bytes
     * @param bytesProcessed Bytes procesados del archivo actual
     */
    public void updateCurrentFileProgress(String fileName, long fileSize, long bytesProcessed) {
        this.currentFileName = fileName;
        this.currentFileSize = fileSize;
     // Evitar división por cero y valores negativos
        if (fileSize > 0) {
            this.currentFileProgress = Math.max(0, Math.min(100, (int) ((bytesProcessed * 100) / fileSize)));
        } else {
            this.currentFileProgress = 0;
        }

        // Corregir el cálculo de processedBytes
        long newProcessedBytes = processedBytes + bytesProcessed;
        this.processedBytes = Math.max(0, Math.min(totalBytes, newProcessedBytes));

        // Calcular tiempo restante
        calculateRemainingTime();
    }

    /**
     * Marca el archivo actual como completado
     */
    public void completeCurrentFile() {
        this.processedFiles++;
        this.processedBytes += currentFileSize;
        this.currentFileProgress = 100;
        calculateRemainingTime();
    }

    /**
     * Calcula el tiempo estimado restante
     */
    private void calculateRemainingTime() {
        if (processedBytes <= 0 || totalBytes <= 0) {
            estimatedRemainingTime = 0;
            return;
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        double speed = (double) processedBytes / elapsedTime; // bytes/ms
        long remainingBytes = totalBytes - processedBytes;
        
        estimatedRemainingTime = (long) (remainingBytes / speed);
    }

    /**
     * Actualiza el estado del proceso
     * @param status Nuevo estado
     */
    public void setStatus(String status) {
        this.currentStatus = status;
    }

    // Métodos de acceso (getters)
    public int getTotalFiles() {
        return totalFiles;
    }

    public int getProcessedFiles() {
        return processedFiles;
    }

    public int getCurrentFileProgress() {
        return currentFileProgress;
    }

    public String getCurrentFileName() {
        return currentFileName;
    }

    public int getOverallProgress() {
        if (totalBytes == 0) return 0;
        return (int) ((processedBytes * 100) / totalBytes);
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public String getFormattedRemainingTime() {
        long seconds = estimatedRemainingTime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%d h %02d min", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d min %02d s", minutes, seconds % 60);
        } else {
            return String.format("%d s", seconds);
        }
    }

    public String getProcessedSize() {
        return formatFileSize(processedBytes);
    }

    public String getTotalSize() {
        return formatFileSize(totalBytes);
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char unit = "KMGTPE".charAt(exp-1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), unit);
    }
}