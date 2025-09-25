package talentcapitalme.com.comparatio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Service for file storage and retrieval operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${app.file-storage.base-path:./uploads}")
    private String basePath;

    @Value("${app.file-storage.retention-days:90}")
    private int retentionDays;

    /**
     * Store uploaded file
     */
    public String storeUploadedFile(MultipartFile file, String clientId, String batchId) throws IOException {
        log.info("Storing uploaded file for client: {}, batch: {}", clientId, batchId);
        
        // Create client-specific directory
        Path clientDir = createClientDirectory(clientId);
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String storedFilename = generateStoredFilename(clientId, batchId, fileExtension);
        
        // Store file
        Path targetLocation = clientDir.resolve(storedFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("File stored successfully: {}", targetLocation);
        return targetLocation.toString();
    }

    /**
     * Store result file
     */
    public String storeResultFile(byte[] resultData, String clientId, String batchId, String fileExtension) throws IOException {
        log.info("Storing result file for client: {}, batch: {}", clientId, batchId);
        
        // Create client-specific directory
        Path clientDir = createClientDirectory(clientId);
        
        // Generate result filename
        String resultFilename = generateResultFilename(clientId, batchId, fileExtension);
        
        // Store file
        Path targetLocation = clientDir.resolve(resultFilename);
        Files.write(targetLocation, resultData);
        
        log.info("Result file stored successfully: {}", targetLocation);
        return targetLocation.toString();
    }

    /**
     * Retrieve file as resource
     */
    public Resource loadFileAsResource(String filePath) throws IOException {
        log.debug("Loading file as resource: {}", filePath);
        
        Path file = Paths.get(filePath);
        Resource resource = new UrlResource(file.toUri());
        
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new IOException("File not found or not readable: " + filePath);
        }
    }

    /**
     * Delete file
     */
    public boolean deleteFile(String filePath) {
        try {
            log.info("Deleting file: {}", filePath);
            Path file = Paths.get(filePath);
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            log.error("Error deleting file: {}", filePath, e);
            return false;
        }
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * Get file size
     */
    public long getFileSize(String filePath) throws IOException {
        return Files.size(Paths.get(filePath));
    }

    /**
     * Clean up expired files
     */
    public int cleanupExpiredFiles() {
        log.info("Starting cleanup of expired files");
        
        try {
            Path baseDir = Paths.get(basePath);
            if (!Files.exists(baseDir)) {
                return 0;
            }
            
            int deletedCount = 0;
            Instant cutoffTime = Instant.now().minusSeconds(retentionDays * 24 * 60 * 60);
            
            // Walk through all files and delete expired ones
            Files.walk(baseDir)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            if (Files.getLastModifiedTime(file).toInstant().isBefore(cutoffTime)) {
                                Files.delete(file);
                                log.debug("Deleted expired file: {}", file);
                            }
                        } catch (IOException e) {
                            log.error("Error deleting expired file: {}", file, e);
                        }
                    });
            
            log.info("Cleanup completed. Deleted {} files", deletedCount);
            return deletedCount;
        } catch (IOException e) {
            log.error("Error during file cleanup", e);
            return 0;
        }
    }

    /**
     * Create client-specific directory
     */
    private Path createClientDirectory(String clientId) throws IOException {
        Path clientDir = Paths.get(basePath, "clients", clientId);
        Files.createDirectories(clientDir);
        return clientDir;
    }

    /**
     * Generate stored filename
     */
    private String generateStoredFilename(String clientId, String batchId, String fileExtension) {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(Instant.now());
        return String.format("upload_%s_%s_%s%s", clientId, batchId, timestamp, fileExtension);
    }

    /**
     * Generate result filename
     */
    private String generateResultFilename(String clientId, String batchId, String fileExtension) {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(Instant.now());
        return String.format("result_%s_%s_%s%s", clientId, batchId, timestamp, fileExtension);
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".xlsx";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Get file retention period
     */
    public int getRetentionDays() {
        return retentionDays;
    }

    /**
     * Calculate expiration date
     */
    public Instant calculateExpirationDate() {
        return Instant.now().plusSeconds(retentionDays * 24 * 60 * 60);
    }
}
