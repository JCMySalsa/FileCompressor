package com.compressor.model; // Define el paquete del modelo

import java.io.*; // Importa clases para manejar archivos
import java.util.List; // Importa la clase List para manejar listas de archivos
import java.util.concurrent.*; // Importa clases para manejo de concurrencia
import java.util.zip.*; // Importa clases para manejar archivos ZIP

public class FileCompressor { // Clase que maneja la compresión de archivos
    private List<File> filesToCompress; // Lista de archivos a comprimir
    private String outputPath; // Ruta del archivo ZIP de salida
    private CompressionListener listener; // Listener para notificar el progreso

    public interface CompressionListener { // Interfaz para manejar eventos de compresión
        void onProgressUpdate(int fileIndex, int progress); // Se llama cuando hay progreso
        void onFileComplete(int fileIndex); // Se llama cuando un archivo se completa
        void onCompressionComplete(); // Se llama cuando la compresión termina
        void onError(File file, Exception e); // Se llama cuando ocurre un error
    }

    public FileCompressor() {} // Constructor vacío

    public void setFilesToCompress(List<File> files) { // Establece los archivos a comprimir
        this.filesToCompress = files;
    }

    public void setOutputPath(String path) { // Establece la ruta de salida del ZIP
        this.outputPath = path;
    }

    public void setCompressionListener(CompressionListener listener) { // Asigna el listener
        this.listener = listener;
    }

    public boolean startCompression() { // Inicia la compresión de archivos
        if (filesToCompress == null || filesToCompress.isEmpty() || outputPath == null) { // Verifica que haya archivos y ruta
            return false;
        }
        
        try (FileOutputStream fos = new FileOutputStream(outputPath); // Crea el archivo ZIP
             ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos))) {
            
            for (int i = 0; i < filesToCompress.size(); i++) {
                try {
                    compressSingleFile(filesToCompress.get(i), zos, i); // Comprime cada archivo
                } catch (IOException e) {
                    if (listener != null) {
                        listener.onError(filesToCompress.get(i), e); // Notifica si hay error
                    }
                    return false;
                }
            }
            
            if (listener != null) {
                listener.onCompressionComplete(); // Notifica que la compresión terminó
            }
            return true;
        } catch (IOException e) {
            if (listener != null) {
                listener.onError(null, e); // Notifica error general
            }
            return false;
        }
    }

    private void compressSingleFile(File file, ZipOutputStream zos, int fileIndex) throws IOException { // Comprime un archivo
        byte[] buffer = new byte[8192]; // Tamaño del buffer de lectura
        int bytesRead;
        long totalBytes = file.length(); // Tamaño total del archivo
        long processedBytes = 0; // Bytes procesados hasta ahora

        ZipEntry zipEntry = new ZipEntry(file.getName()); // Crea una entrada ZIP para el archivo
        zos.putNextEntry(zipEntry);
        
        try (FileInputStream fis = new FileInputStream(file)) { // Lee el archivo
            while ((bytesRead = fis.read(buffer)) != -1) {
                zos.write(buffer, 0, bytesRead); // Escribe en el ZIP
                processedBytes += bytesRead;
                int progress = (int) ((processedBytes * 100) / totalBytes); // Calcula el progreso
                
                if (listener != null) {
                    listener.onProgressUpdate(fileIndex, progress); // Notifica el progreso
                }
            }
        }
        zos.closeEntry(); // Cierra la entrada ZIP
        
        if (listener != null) {
            listener.onFileComplete(fileIndex); // Notifica que el archivo se completó
        }
    }
}
