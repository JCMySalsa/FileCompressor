package com.compressor.controller;

import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Controlador encargado de manejar la compresión de archivos en hilos separados.
 * Utiliza un ExecutorService para gestionar múltiples tareas de compresión simultáneamente.
 */
public class FileCompressionController {
    private ExecutorService executorService; // Pool de hilos para manejar la compresión
    private CompressionProgressListener listener; // Interfaz para actualizar la UI con el progreso
    private final Object zipLock = new Object(); // Objeto para sincronizar el acceso al ZIP
    
    /**
     * Constructor que inicializa el controlador con un listener para actualizar el progreso.
     * @param listener Interfaz para recibir actualizaciones de progreso.
     */
    public FileCompressionController(CompressionProgressListener listener) {
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.listener = listener;
    }

    /**
     * Método para iniciar la compresión de una lista de archivos en un solo ZIP.
     * @param files Lista de archivos a comprimir.
     * @param outputZipPath Ruta de salida del archivo ZIP.
     */
    public void compressFiles(List<File> files, String outputZipPath) {
        try (FileOutputStream fos = new FileOutputStream(outputZipPath);
             ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos))) {
            
            for (File file : files) {
                executorService.submit(() -> compressFile(file, zos));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método que comprime un archivo individualmente y actualiza el progreso.
     * Se usa sincronización para evitar accesos simultáneos al ZipOutputStream.
     * @param file Archivo a comprimir.
     * @param zos OutputStream compartido para escribir en el ZIP.
     */
    private void compressFile(File file, ZipOutputStream zos) {
        try {
            byte[] buffer = new byte[1024];
            int bytesRead;
            long totalBytes = file.length();
            long compressedBytes = 0;

            try (FileInputStream fis = new FileInputStream(file)) {
                synchronized (zipLock) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);
                }

                while ((bytesRead = fis.read(buffer)) != -1) {
                    synchronized (zipLock) {
                        zos.write(buffer, 0, bytesRead);
                    }
                    compressedBytes += bytesRead;
                    int progress = (int) ((compressedBytes * 100) / totalBytes);
                    listener.onProgressUpdate(file, progress);
                }

                synchronized (zipLock) {
                    zos.closeEntry(); // Cierra la entrada del archivo en el ZIP
                }
            }
            listener.onCompressionComplete(file);
        } catch (IOException e) {
            listener.onCompressionError(file, e);
        }
    }

    /**
     * Método para cerrar el ExecutorService y liberar los recursos.
     */
    public void shutdown() {
        executorService.shutdown();
    }
}

/**
 * Interfaz para manejar las actualizaciones de progreso y errores durante la compresión.
 */
interface CompressionProgressListener {
    void onProgressUpdate(File file, int progress); // Actualiza el progreso de compresión
    void onCompressionComplete(File file); // Notifica cuando la compresión ha terminado
    void onCompressionError(File file, Exception e); // Maneja errores de compresión
}
