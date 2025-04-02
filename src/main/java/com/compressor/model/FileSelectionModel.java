package com.compressor.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Modelo para manejar la selección de archivos
 */
public class FileSelectionModel {
    private List<File> selectedFiles;
    private File lastDirectory;
    
    public FileSelectionModel(){
        this.selectedFiles = new ArrayList<>();
        this.lastDirectory = new File(System.getProperty(
                "user.home"));
    }
    
    public boolean showFileSelectionDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(lastDirectory);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Archivos comunes (*.txt, *.pdf, *.jpg, *.png)", 
            "txt", "pdf", "jpg", "png", "doc", "docx", "xls", "xlsx");
        
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(null);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            this.lastDirectory = fileChooser.getCurrentDirectory();
            addFiles(List.of(fileChooser.getSelectedFiles()));
            return true;
        }
        return false;
    }
    
    /**
     * Agrega archivos a la selección
     * @param files Archivos seleccionados
     */
    public void addFiles(List<File> files) {
        if (files != null) {
            for (File file : files) {
                if (!selectedFiles.contains(file)) {
                    selectedFiles.add(file);
                }
                }
            }
        }
    
    /**
     * Elimina un archivo de la selección
     * @param file Archivo a remover
     */
    public boolean removeFile(File file) {
       return selectedFiles.remove(file);
    }
    
    public void removeFileIndex(int[] indices) {
        for(int i = indices.length - 1; i >= 0; i--) {
            if (indices[i] >= 0 && indices[i] < selectedFiles.size()) {
                selectedFiles.remove(indices[i]);
            }
        }
    }
    
    /**
     * Obtiene los archivos seleccionados
     * @return Lista de archivos
     */
    public List<File> getSelectedFiles() {       
        return List.copyOf(selectedFiles);
    }
    
    /*
     * Obtiene los nombres de los archivos seleccionados
     * @return Array de nombres
     */
    
    public String[] getSelectedFileNames() {
        return selectedFiles.stream().map(File::getName).
                toArray(String[]::new);
    }
    
    /**
     * Limpia la selección actual
     */
    public void clearSelection() {
        selectedFiles.clear();
    }
    
    /*
     * Verifica si hay archivos seleccionados
     * @return true si hay archivos seleccionados
     * 
     */
    public boolean hasFiles() {
        return !selectedFiles.isEmpty();
    }
    
    /*
     * Obtiene el tamanio total de los archivos seleccionados
     * @return tamanio total en bytes
     */
    
    public long getTotalSize() {
        return selectedFiles.stream().mapToLong(File::length).sum();
    }
    
    /*
     * Obtiene el tamanio total formateado (kb, mb, gb)
     * @return Cadena con el tamnio formateado
     */
    public String getFormattedTotalSize() {
        long bytes = getTotalSize();
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes)/Math.log(1024));
        char unit = "KMGTPE".charAt(exp-1);
        return String.format("%.1f %sB" , bytes / Math.pow(1024, exp),unit);
    }
    /*
     * Obtiene la cantidad de archivos seleccionados
     * @return Numero de archivos
     */
    public int getFileCount() {
        return selectedFiles.size();}
}