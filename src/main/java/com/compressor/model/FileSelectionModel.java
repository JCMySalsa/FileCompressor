package com.compressor.model; // Define el paquete donde se encuentra la clase

import java.io.File; // Importa la clase File para representar archivos en el sistema
import java.util.ArrayList; // Importa ArrayList para manejar una lista dinámica de archivos
import java.util.List; // Importa la interfaz List para manejar listas genéricas
import javax.swing.JFileChooser; // Importa JFileChooser para permitir la selección de archivos en una ventana emergente
import javax.swing.filechooser.FileNameExtensionFilter; // Importa un filtro para limitar los tipos de archivo que se pueden seleccionar

/**
 * Clase que maneja la selección de archivos mediante un cuadro de diálogo.
 */
public class FileSelectionModel {
    private List<File> selectedFiles; // Lista donde se almacenan los archivos seleccionados por el usuario
    private File lastDirectory; // Último directorio que el usuario usó al seleccionar archivos

    /**
     * Constructor que inicializa la lista de archivos seleccionados y el directorio inicial.
     */
    public FileSelectionModel() {
        this.selectedFiles = new ArrayList<>(); // Crea una lista vacía para almacenar archivos seleccionados
        this.lastDirectory = new File(System.getProperty("user.home")); // Establece el directorio inicial como la carpeta del usuario
    }

    /**
     * Muestra un cuadro de diálogo para que el usuario seleccione archivos.
     * 
     * @return true si el usuario selecciona archivos, false si cancela la selección.
     */
    public boolean showFileSelectionDialog() {
        JFileChooser fileChooser = new JFileChooser(); // Crea un selector de archivos
        fileChooser.setCurrentDirectory(lastDirectory); // Establece el último directorio usado como directorio inicial
        fileChooser.setMultiSelectionEnabled(true); // Permite la selección de múltiples archivos
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY); // Restringe la selección solo a archivos, no carpetas

        // Crea un filtro para que solo se puedan seleccionar ciertos tipos de archivos
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Archivos comunes (*.txt, *.pdf, *.jpg, *.png, etc.)", "txt", "pdf", "jpg", "png", "doc", "docx", "xls", "xlsx"
        );
        fileChooser.setFileFilter(filter); // Aplica el filtro al selector de archivos

        // Abre el cuadro de diálogo y espera la respuesta del usuario
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) { // Si el usuario selecciona archivos y confirma
            this.lastDirectory = fileChooser.getCurrentDirectory(); // Guarda el directorio donde se seleccionaron los archivos
            addFiles(List.of(fileChooser.getSelectedFiles())); // Se llena la lista List<File> files con los archivos seleccionados por el usuario
            return true; // Indica que se seleccionaron archivos exitosamente
        }
        return false; // Indica que el usuario canceló la selección
    }

    /**
     * Agrega una lista de archivos a la lista interna, evitando duplicados.
     * 
     * @param files Lista de archivos seleccionados (se llena en showFileSelectionDialog)
     */
    public void addFiles(List<File> files) {
        if (files != null) { // Verifica que la lista no sea nula
            for (File file : files) { // Recorre cada archivo en la lista
                if (!selectedFiles.contains(file)) { // Solo agrega archivos que no estén ya en la lista
                    selectedFiles.add(file); // Agrega el archivo a la lista interna
                }
            }
        }
    }

    /**
     * Elimina un archivo específico de la lista de archivos seleccionados.
     * 
     * @param file Archivo a eliminar
     * @return true si se eliminó correctamente, false si el archivo no estaba en la lista
     */
    public boolean removeFile(File file) {
        return selectedFiles.remove(file);
    }

    /**
     * Elimina archivos según sus índices en la lista interna.
     * 
     * @param indices Array con los índices de los archivos a eliminar
     */
    public void removeFileIndex(int[] indices) {
        for (int i = indices.length - 1; i >= 0; i--) { // Se eliminan en orden inverso para evitar problemas con los índices
            if (indices[i] >= 0 && indices[i] < selectedFiles.size()) {
                selectedFiles.remove(indices[i]);
            }
        }
    }

    /**
     * Obtiene la lista de archivos seleccionados.
     * 
     * @return Una copia de la lista de archivos seleccionados
     */
    public List<File> getSelectedFiles() {
        return List.copyOf(selectedFiles); // Retorna una copia inmutable de la lista para evitar modificaciones externas
    }

    /**
     * Obtiene los nombres de los archivos seleccionados.
     * 
     * @return Un array con los nombres de los archivos
     */
    public String[] getSelectedFileNames() {
        return selectedFiles.stream()
                             .map(File::getName) // Convierte cada archivo en su nombre
                             .toArray(String[]::new); // Devuelve un array con los nombres
    }

    /**
     * Elimina todos los archivos de la lista de selección.
     */
    public void clearSelection() {
        selectedFiles.clear();
    }

    /**
     * Verifica si hay archivos seleccionados en la lista.
     * 
     * @return true si hay archivos en la lista, false si está vacía
     */
    public boolean hasFiles() {
        return !selectedFiles.isEmpty();
    }

    /**
     * Calcula el tamaño total de los archivos seleccionados.
     * 
     * @return Tamaño total en bytes
     */
    public long getTotalSize() {
        return selectedFiles.stream().mapToLong(File::length).sum(); // Suma el tamaño de cada archivo en la lista
    }

    /**
     * Obtiene el tamaño total de los archivos formateado en KB, MB o GB.
     * 
     * @return Cadena con el tamaño formateado
     */
    public String getFormattedTotalSize() {
        long bytes = getTotalSize();
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024)); // Determina la unidad de medida (KB, MB, GB, etc.)
        char unit = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), unit); // Formatea el tamaño con una sola decimal
    }

    /**
     * Obtiene la cantidad total de archivos seleccionados.
     * 
     * @return Número total de archivos en la lista
     */
    public int getFileCount() {
        return selectedFiles.size(); // Devuelve el número de archivos en la lista
    }
}
