package talentcapitalme.com.comparatio.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;

/**
 * Interface for File Storage Service operations
 */
public interface IFileStorageService {
    
    /**
     * Store uploaded file
     */
    String storeUploadedFile(MultipartFile file, String clientId, String batchId) throws IOException;
    
    /**
     * Store result file
     */
    String storeResultFile(byte[] resultData, String clientId, String batchId, String fileExtension) throws IOException;
    
    /**
     * Retrieve file as resource
     */
    Resource loadFileAsResource(String filePath) throws IOException;
    
    /**
     * Delete file
     */
    boolean deleteFile(String filePath);
    
    /**
     * Check if file exists
     */
    boolean fileExists(String filePath);
    
    /**
     * Get file size
     */
    long getFileSize(String filePath) throws IOException;
    
    /**
     * Clean up expired files
     */
    int cleanupExpiredFiles();
    
    /**
     * Get file retention period
     */
    int getRetentionDays();
    
    /**
     * Calculate expiration date
     */
    Instant calculateExpirationDate();
    
    /**
     * Store profile image
     */
    String storeProfileImage(MultipartFile file, String userId) throws IOException;
    
    /**
     * Generate unique avatar URL for a user
     */
    String generateUniqueAvatarUrl(String userId, String fileExtension);
    
    /**
     * Get the base directory for profile images
     */
    String getProfileImagesBaseDirectory();
}
