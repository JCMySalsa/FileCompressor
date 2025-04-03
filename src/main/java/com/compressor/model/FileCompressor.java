package com.compressor.model; // Define el paquete donde se encuentra la clase

import java.io.*; // Importa clases para manejo de archivos
import java.util.List; // Importa la interfaz List para manejar listas de archivos
import java.util.ArrayList; // Importa ArrayList para implementar la lista de archivos
import java.util.zip.*; // Importa clases para la compresión de archivos en formato ZIP

public class FileCompressor {
    private List<File> filesToCompress; // Lista de archivos que se van a comprimir
    private String outputPath; // Ruta donde se guardará el archivo ZIP resultante
    private CompressionListener listener; // Listener para manejar eventos durante la compresión

    // Interfaz para definir eventos relacionados con la compresión
    public interface CompressionListener {
        void onProgressUpdate(int fileIndex, int progress); // Evento para actualizar progreso de un archivo
        void onFileComplete(int fileIndex); // Evento cuando un archivo ha sido comprimido completamente
        void onCompressionComplete(); // Evento cuando la compresión de todos los archivos ha finalizado
        void onError(File file, Exception e); // Evento cuando ocurre un error en la compresión
    }

    public FileCompressor() {} // Constructor vacío

    /**
     * Establece la lista de archivos a comprimir.
     * @param files Lista de archivos que serán comprimidos
     */
    public void setFilesToCompress(List<File> files) {
        this.filesToCompress = files;
    }

    /**
     * Define la ruta donde se guardará el archivo ZIP resultante.
     * @param path Ruta de salida del archivo ZIP
     */
    public void setOutputPath(String path) {
        this.outputPath = path;
    }

    /**
     * Asigna un listener que manejará los eventos de la compresión.
     * @param listener Objeto que implementa CompressionListener
     */
    public void setCompressionListener(CompressionListener listener) {
        this.listener = listener;
    }

    /**
     * Inicia el proceso de compresión de los archivos.
     * @return true si la compresión fue exitosa, false si hubo un problema
     */
    public boolean startCompression() {
        if (filesToCompress == null || filesToCompress.isEmpty() || outputPath == null) {
            return false; // No hay archivos que comprimir o no hay ruta de salida definida
        }

        List<Thread> threads = new ArrayList<>(); // Lista para manejar los hilos de compresión

        try (FileOutputStream fos = new FileOutputStream(outputPath); // Archivo ZIP de salida
             ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos))) {

            // Itera sobre cada archivo y lo comprime en un hilo independiente
            for (int i = 0; i < filesToCompress.size(); i++) {
                final int index = i; // Índice del archivo actual
                final File file = filesToCompress.get(i);

                // Crea un nuevo hilo para comprimir el archivo
                Thread thread = new Thread(() -> {
                    try {
                        compressSingleFile(file, zos, index);
                    } catch (IOException e) {
                        if (listener != null) {
                            listener.onError(file, e); // Notifica si ocurre un error durante la compresión
                        }
                    }
                });

                threads.add(thread); // Agrega el hilo a la lista
                thread.start(); // Inicia el hilo
            }

            // Espera a que todos los hilos terminen
            for (Thread thread : threads) {
                thread.join(); // Espera a que el hilo termine antes de continuar
            }

            // Notifica que la compresión ha finalizado completamente
            if (listener != null) {
                listener.onCompressionComplete();
            }
            return true;

        } catch (IOException | InterruptedException e) {
            if (listener != null) {
                listener.onError(null, e); // Notifica un error general si ocurre
            }
            return false;
        }
    }

    /**
     * Comprime un solo archivo y lo agrega al ZIP.
     * @param file Archivo a comprimir
     * @param zos Stream de salida ZIP
     * @param fileIndex Índice del archivo en la lista
     * @throws IOException Si ocurre un error durante la compresión
     */
    private void compressSingleFile(File file, ZipOutputStream zos, int fileIndex) throws IOException {
        byte[] buffer = new byte[8192]; // Buffer para lectura de archivos en bloques
        int bytesRead;
        long totalBytes = file.length(); // Tamaño total del archivo
        long processedBytes = 0; // Bytes ya procesados

        synchronized (zos) { // Se sincroniza para evitar conflictos al escribir en el ZIP
            ZipEntry zipEntry = new ZipEntry(file.getName()); // Crea una entrada ZIP para el archivo
            zos.putNextEntry(zipEntry);

            try (FileInputStream fis = new FileInputStream(file)) {
                while ((bytesRead = fis.read(buffer)) != -1) { // Lee el archivo en bloques
                    zos.write(buffer, 0, bytesRead); // Escribe los datos en el ZIP
                    processedBytes += bytesRead; // Actualiza la cantidad de datos procesados
                    int progress = (int) ((processedBytes * 100) / totalBytes); // Calcula el progreso

                    // Notifica el progreso de la compresión
                    if (listener != null) {
                        listener.onProgressUpdate(fileIndex, progress);
                    }
                }
            }
            zos.closeEntry(); // Cierra la entrada del archivo en el ZIP

            // Notifica que la compresión de este archivo ha finalizado
            if (listener != null) {
                listener.onFileComplete(fileIndex);
            }
        }
    }
}
