package talentcapitalme.com.comparatio.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import talentcapitalme.com.comparatio.entity.UploadHistory;

import java.util.List;
import java.util.Optional;

/**
 * Interface for Upload History Service operations
 */
public interface IUploadHistoryService {
    
    /**
     * Create upload history record
     */
    UploadHistory createUploadHistory(String clientId, String clientName, String originalFileName, 
                                   String uploadedFileName, String batchId, String uploadedBy, 
                                   String uploadedByEmail);
    
    /**
     * Update upload history with processing results
     */
    UploadHistory updateUploadHistory(String batchId, int totalRows, int processedRows, 
                                   int successRows, int errorRows, long processingTimeMs, 
                                   String resultFilePath, List<String> validationErrors);
    
    /**
     * Update upload history with file paths
     */
    UploadHistory updateFilePaths(String batchId, String uploadFilePath, String resultFilePath);
    
    /**
     * Mark upload as failed
     */
    UploadHistory markUploadFailed(String batchId, String errorMessage);
    
    /**
     * Get upload history by client ID
     */
    List<UploadHistory> getUploadHistoryByClient(String clientId);
    
    /**
     * Get upload history by client ID with pagination
     */
    Page<UploadHistory> getUploadHistoryByClient(String clientId, Pageable pageable);
    
    /**
     * Get upload history by batch ID
     */
    Optional<UploadHistory> getUploadHistoryByBatch(String batchId);
    
    /**
     * Get upload history by status
     */
    List<UploadHistory> getUploadHistoryByStatus(UploadHistory.UploadStatus status);
    
    /**
     * Get upload history by client and status
     */
    List<UploadHistory> getUploadHistoryByClientAndStatus(String clientId, UploadHistory.UploadStatus status);
    
    /**
     * Get recent uploads
     */
    List<UploadHistory> getRecentUploads(String clientId, int days);
    
    /**
     * Search uploads by filename
     */
    List<UploadHistory> searchUploadsByFilename(String clientId, String filenamePattern);
    
    /**
     * Get upload statistics for client
     */
    UploadStatistics getUploadStatistics(String clientId);
    
    /**
     * Clean up expired files
     */
    int cleanupExpiredFiles();
    
    /**
     * Upload statistics DTO
     */
    class UploadStatistics {
        private int totalUploads;
        private int successfulUploads;
        private int failedUploads;
        private int totalRows;
        private int successRows;
        private int errorRows;
        private long totalProcessingTimeMs;
        private long averageProcessingTimeMs;
        
        // Getters and setters
        public int getTotalUploads() { return totalUploads; }
        public void setTotalUploads(int totalUploads) { this.totalUploads = totalUploads; }
        
        public int getSuccessfulUploads() { return successfulUploads; }
        public void setSuccessfulUploads(int successfulUploads) { this.successfulUploads = successfulUploads; }
        
        public int getFailedUploads() { return failedUploads; }
        public void setFailedUploads(int failedUploads) { this.failedUploads = failedUploads; }
        
        public int getTotalRows() { return totalRows; }
        public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
        
        public int getSuccessRows() { return successRows; }
        public void setSuccessRows(int successRows) { this.successRows = successRows; }
        
        public int getErrorRows() { return errorRows; }
        public void setErrorRows(int errorRows) { this.errorRows = errorRows; }
        
        public long getTotalProcessingTimeMs() { return totalProcessingTimeMs; }
        public void setTotalProcessingTimeMs(long totalProcessingTimeMs) { this.totalProcessingTimeMs = totalProcessingTimeMs; }
        
        public long getAverageProcessingTimeMs() { return averageProcessingTimeMs; }
        public void setAverageProcessingTimeMs(long averageProcessingTimeMs) { this.averageProcessingTimeMs = averageProcessingTimeMs; }
    }
}
