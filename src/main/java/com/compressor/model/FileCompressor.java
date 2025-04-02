package com.compressor.model;

import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Clase modelo que maneja la lógica de compresión de archivos
 */
public class FileCompressor {
    private List<File> filesToCompress;
    private String outputPath;
    private ExecutorService executor;
    private CompressionListener listener;
    private final Object zipLock = new Object();
    
    public interface CompressionListener {
        void onProgressUpdate(int fileIndex, int progress);
        void onFileComplete(int fileIndex);
        void onCompressionComplete();
        void onError(File file, Exception e);        
    }
    
    /*  
     * Constructor que inicializa el executor
     */
    
    public FileCompressor() {
        this.executor = Executors.newFixedThreadPool
                (Runtime.getRuntime().availableProcessors());
    }
    
    
    /**
     * Establece los archivos a comprimir
     * @param files Lista de archivos
     */
    public void setFilesToCompress(List<File> files) {
        this.filesToCompress = files;
    }
    
    /**
     * Establece la ruta de salida para el archivo ZIP
     * @param path Ruta de destino
     */
    public void setOutputPath(String path) {
        this.outputPath = path;
    }
    
    public void setCompressionListener(CompressionListener listener) {
        this.listener = listener;
    }
    
    /**
     * Inicia el proceso de compresión
     * @return true si la compresión fue exitosa
     */
    public boolean startCompression() {
        if (filesToCompress == null || filesToCompress.isEmpty()
                || outputPath == null) {
            return false;
        }
    
        try (FileOutputStream fos = new FileOutputStream(outputPath); 
                ZipOutputStream zos = new ZipOutputStream(fos)){
            for (int i = 0; i < filesToCompress.size(); i++) {
                final int fileIndex = i;
                executor.submit(() -> {
                    try {
                        compressSingleFile(filesToCompress.get(fileIndex), zos, fileIndex);
                    } catch (IOException e) {
                        if (listener != null){   
                    listener.onError(filesToCompress.get(fileIndex), e);   
                }
                    }
                });
            }
        }catch (IOException e){
            if (listener != null) {
                listener.onError(null, e);
            }
            return false;
        }
        
        executor.shutdown();
        return true;
        
    }
    
    /**
     * Comprime un archivo idividual
     * 
     */
    private void compressSingleFile(File file, 
            ZipOutputStream zos, int fileIndex) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        long totalBytes = file.length();
        long processedBytes = 0;
        
        try (FileInputStream fis = new FileInputStream(file)){
            synchronized (zipLock) {
                zos.putNextEntry(new ZipEntry(file.getName()));
            }
            while ((bytesRead = fis.read(buffer)) != -1) {
                synchronized (zipLock) {
                    zos.write(buffer, 0, bytesRead);
                }
                processedBytes += bytesRead;
                int progress = (int) ((processedBytes * 100) / totalBytes);
                if (listener != null) {
                    listener.onProgressUpdate(fileIndex, progress);
                }
            }
            synchronized (zipLock) {
                zos.closeEntry();
            }
        }
        if (listener != null) {
            listener.onFileComplete(fileIndex);
        if(fileIndex == filesToCompress.size() - 1) {
            listener.onCompressionComplete();           
        }
        }
        
    }
    
    public void cancelCompression() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
    
    /**
     * Calcula el progreso actual de la compresión
     * @return Porcentaje completado (0-100)
     */
    public int getCompressionProgress() {
        if (filesToCompress == null || filesToCompress.isEmpty()) {
            return 0;
        }
        
        long totalSize = filesToCompress.stream().mapToLong(File::length).sum();
        long processedSize = 0;
        return (int) ((processedSize * 100)/ totalSize);        
    }
    
    
}