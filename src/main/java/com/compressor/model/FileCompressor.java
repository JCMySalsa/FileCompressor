package com.compressor.model;

import java.io.*; 
import java.util.List; 
import java.util.ArrayList; 
import java.util.zip.*; 

public class FileCompressor {
    private List<File> filesToCompress; // Lista de archivos a comprimir
    private String outputPath; // Ruta del archivo ZIP de salida
    private CompressionListener listener; // Listener para manejar los eventos de progreso

    // Interfaz que define los métodos para escuchar los eventos de la compresión
    public interface CompressionListener {
        void onProgressUpdate(int fileIndex, int progress); // Actualiza el progreso de compresión
        void onFileComplete(int fileIndex); // Notifica cuando un archivo termina de comprimir
        void onCompressionComplete(); // Notifica cuando todos los archivos han sido comprimidos
        void onError(File file, Exception e); // Notifica si ocurre un error durante la compresión
    }

    public FileCompressor() {}

    // Establece los archivos a comprimir
    public void setFilesToCompress(List<File> files) {
        this.filesToCompress = files;
    }

    // Establece la ruta de salida del archivo ZIP
    public void setOutputPath(String path) {
        this.outputPath = path;
    }

    // Asigna el listener que manejará los eventos de progreso
    public void setCompressionListener(CompressionListener listener) {
        this.listener = listener;
    }

    // Inicia la compresión de archivos
    public boolean startCompression() {
        // Verifica que los archivos y la ruta de salida estén definidos
        if (filesToCompress == null || filesToCompress.isEmpty() || outputPath == null) {
            return false;
        }

        List<Thread> threads = new ArrayList<>();

        try (FileOutputStream fos = new FileOutputStream(outputPath); 
             ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos))) {

            // Comprime cada archivo en un hilo independiente
            for (int i = 0; i < filesToCompress.size(); i++) {
                final int index = i;
                final File file = filesToCompress.get(i);

                // Crea y lanza un hilo para cada archivo
                Thread thread = new Thread(() -> {
                    try {
                        compressSingleFile(file, zos, index);
                    } catch (IOException e) {
                        if (listener != null) {
                            listener.onError(file, e); // Notifica si ocurre un error con el archivo
                        }
                    }
                });

                threads.add(thread); // Agrega el hilo a la lista de hilos
                thread.start(); // Inicia el hilo
            }

            // Espera a que todos los hilos terminen su trabajo
            for (Thread thread : threads) {
                thread.join(); // Bloquea hasta que el hilo termine
            }

            // Notifica que la compresión ha finalizado
            if (listener != null) {
                listener.onCompressionComplete();
            }
            return true;

        } catch (IOException | InterruptedException e) {
            if (listener != null) {
                listener.onError(null, e); // Notifica cualquier error general
            }
            return false;
        }
    }

    // Comprime un archivo individual y notifica el progreso
    private void compressSingleFile(File file, ZipOutputStream zos, int fileIndex) throws IOException {
        byte[] buffer = new byte[8192]; // Buffer para leer el archivo
        int bytesRead;
        long totalBytes = file.length(); // Tamaño total del archivo
        long processedBytes = 0; // Bytes procesados hasta el momento

        // Bloqueo sincronizado para evitar conflictos al escribir en el ZipOutputStream
        synchronized (zos) {
            // Crea una entrada ZIP para el archivo
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zos.putNextEntry(zipEntry);

            // Lee y comprime el archivo
            try (FileInputStream fis = new FileInputStream(file)) {
                while ((bytesRead = fis.read(buffer)) != -1) {
                    zos.write(buffer, 0, bytesRead); // Escribe los datos comprimidos
                    processedBytes += bytesRead; // Actualiza los bytes procesados
                    int progress = (int) ((processedBytes * 100) / totalBytes); // Calcula el porcentaje de progreso

                    // Notifica el progreso de la compresión
                    if (listener != null) {
                        listener.onProgressUpdate(fileIndex, progress);
                    }
                }
            }
            zos.closeEntry(); // Cierra la entrada ZIP

            // Notifica que la compresión del archivo ha finalizado
            if (listener != null) {
                listener.onFileComplete(fileIndex);
            }
        }
    }
}

