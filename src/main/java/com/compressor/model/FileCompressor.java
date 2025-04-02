package com.compressor.model;

import java.io.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.zip.*;

public class FileCompressor {
    private List<File> filesToCompress;
    private String outputPath;
    private CompressionListener listener;

    public interface CompressionListener {
        void onProgressUpdate(int fileIndex, int progress);
        void onFileComplete(int fileIndex);
        void onCompressionComplete();
        void onError(File file, Exception e);
    }

    public FileCompressor() {}

    public void setFilesToCompress(List<File> files) {
        this.filesToCompress = files;
    }

    public void setOutputPath(String path) {
        this.outputPath = path;
    }

    public void setCompressionListener(CompressionListener listener) {
        this.listener = listener;
    }

    public boolean startCompression() {
        if (filesToCompress == null || filesToCompress.isEmpty() || outputPath == null) {
            return false;
        }

        try (FileOutputStream fos = new FileOutputStream(outputPath);
             ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos))) {

            // Process files sequentially to avoid ZIP entry conflicts
            for (int i = 0; i < filesToCompress.size(); i++) {
                try {
                    compressSingleFile(filesToCompress.get(i), zos, i);
                } catch (IOException e) {
                    if (listener != null) {
                        listener.onError(filesToCompress.get(i), e);
                    }
                    return false;
                }
            }

            if (listener != null) {
                listener.onCompressionComplete();
            }
            return true;
        } catch (IOException e) {
            if (listener != null) {
                listener.onError(null, e);
            }
            return false;
        }
    }

    private void compressSingleFile(File file, ZipOutputStream zos, int fileIndex) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        long totalBytes = file.length();
        long processedBytes = 0;

        // Create ZIP entry
        ZipEntry zipEntry = new ZipEntry(file.getName());
        zos.putNextEntry(zipEntry);

        try (FileInputStream fis = new FileInputStream(file)) {
            while ((bytesRead = fis.read(buffer)) != -1) {
                zos.write(buffer, 0, bytesRead);
                processedBytes += bytesRead;
                int progress = (int) ((processedBytes * 100) / totalBytes);
                if (listener != null) {
                    listener.onProgressUpdate(fileIndex, progress);
                }
            }
        }

        zos.closeEntry();
        
        if (listener != null) {
            listener.onFileComplete(fileIndex);
        }
    }
}