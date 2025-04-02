package com.compressor.model; // Define el paquete del modelo

import java.io.File; // Importa la clase File para manejar archivos
import java.util.ArrayList; // Importa ArrayList para manejar listas de archivos
import java.util.List; // Importa la clase List
import javax.swing.JFileChooser; // Importa JFileChooser para seleccionar archivos
import javax.swing.filechooser.FileNameExtensionFilter; // Filtro para tipos de archivo

/**
 * Modelo para manejar la selección de archivos
 */
public class FileSelectionModel {
    private List<File> selectedFiles; // Lista de archivos seleccionados
    private File lastDirectory; // Último directorio usado

    public FileSelectionModel() { // Constructor que inicializa la lista y el directorio
        this.selectedFiles = new ArrayList<>();
        this.lastDirectory = new File(System.getProperty("user.home")); // Directorio inicial: carpeta del usuario
    }

    public boolean showFileSelectionDialog() { // Muestra el cuadro de diálogo para seleccionar archivos
        JFileChooser fileChooser = new JFileChooser(); // Crea un selector de archivos
        fileChooser.setCurrentDirectory(lastDirectory); // Establece el directorio inicial
        fileChooser.setMultiSelectionEnabled(true); // Permite seleccionar varios archivos
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY); // Solo permite seleccionar archivos

        FileNameExtensionFilter filter = new FileNameExtensionFilter( // Filtro para tipos de archivo
            "Archivos comunes (*.txt, *.pdf, *.jpg, *.png)", "txt", "pdf", "jpg", "png", "doc", "docx", "xls", "xlsx"
        );
        fileChooser.setFileFilter(filter); // Aplica el filtro

        int result = fileChooser.showOpenDialog(null); // Abre el diálogo
        if (result == JFileChooser.APPROVE_OPTION) { // Si el usuario selecciona archivos
            this.lastDirectory = fileChooser.getCurrentDirectory(); // Guarda el directorio
            addFiles(List.of(fileChooser.getSelectedFiles())); // Agrega los archivos seleccionados
            return true;
        }
        return false; // Si no se seleccionan archivos
    }

    /**
     * Agrega archivos a la selección
     * 
     * @param files Archivos seleccionados
     */
    public void addFiles(List<File> files) { //de donde se llena esa lista
        if (files != null) {
            for (File file : files) {
                if (!selectedFiles.contains(file)) { // Evita duplicados
                    selectedFiles.add(file);
                }
            }
        }
    }

    /**
     * Elimina un archivo de la selección
     * 
     * @param file Archivo a remover
     */
    public boolean removeFile(File file) {
        return selectedFiles.remove(file);
    }

    public void removeFileIndex(int[] indices) { // Elimina archivos según índice
        for (int i = indices.length - 1; i >= 0; i--) { // Se elimina en orden inverso para evitar errores
            if (indices[i] >= 0 && indices[i] < selectedFiles.size()) {
                selectedFiles.remove(indices[i]);
            }
        }
    }

    /**
     * Obtiene los archivos seleccionados
     * 
     * @return Lista de archivos
     */
    public List<File> getSelectedFiles() {
        return List.copyOf(selectedFiles); // Devuelve una copia de la lista
    }

    /**
     * Obtiene los nombres de los archivos seleccionados
     * 
     * @return Array de nombres
     */
    public String[] getSelectedFileNames() {
        return selectedFiles.stream()
                             .map(File::getName) // Obtiene los nombres de archivo
                             .toArray(String[]::new);
    }

    /**
     * Limpia la selección actual
     */
    public void clearSelection() {
        selectedFiles.clear();
    }

    /**
     * Verifica si hay archivos seleccionados
     * 
     * @return true si hay archivos seleccionados
     */
    public boolean hasFiles() {
        return !selectedFiles.isEmpty(); // Devuelve true si la lista no está vacía
    }

    /**
     * Obtiene el tamaño total de los archivos seleccionados
     * 
     * @return tamaño total en bytes
     */
    public long getTotalSize() {
        return selectedFiles.stream().mapToLong(File::length).sum(); // Suma los tamaños de los archivos
    }

    /**
     * Obtiene el tamaño total formateado (KB, MB, GB)
     * 
     * @return Cadena con el tamaño formateado
     */
    public String getFormattedTotalSize() {
        long bytes = getTotalSize();
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024)); // Calcula la unidad de tamaño
        char unit = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), unit); // Devuelve el tamaño formateado
    }

    /**
     * Obtiene la cantidad de archivos seleccionados
     * 
     * @return Número de archivos
     */
    public int getFileCount() {
        return selectedFiles.size(); // Devuelve el número de archivos seleccionados
    }
}
