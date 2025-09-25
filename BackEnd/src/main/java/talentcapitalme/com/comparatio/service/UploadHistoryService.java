package talentcapitalme.com.comparatio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import talentcapitalme.com.comparatio.entity.UploadHistory;
import talentcapitalme.com.comparatio.repository.UploadHistoryRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing upload history operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadHistoryService {

    private final UploadHistoryRepository uploadHistoryRepository;
    private final FileStorageService fileStorageService;

    /**
     * Create upload history record
     */
    public UploadHistory createUploadHistory(String clientId, String clientName, String originalFileName, 
                                           String uploadedFileName, String batchId, String uploadedBy, 
                                           String uploadedByEmail) {
        log.info("Creating upload history for client: {}, batch: {}", clientId, batchId);
        
        UploadHistory uploadHistory = UploadHistory.builder()
                .clientId(clientId)
                .clientName(clientName)
                .originalFileName(originalFileName)
                .uploadedFileName(uploadedFileName)
                .batchId(batchId)
                .status(UploadHistory.UploadStatus.UPLOADING)
                .uploadedBy(uploadedBy)
                .uploadedByEmail(uploadedByEmail)
                .expiresAt(fileStorageService.calculateExpirationDate())
                .filesDeleted(false)
                .createdAt(Instant.now())
                .build();
        
        return uploadHistoryRepository.save(uploadHistory);
    }

    /**
     * Update upload history with processing results
     */
    public UploadHistory updateUploadHistory(String batchId, int totalRows, int processedRows, 
                                           int successRows, int errorRows, long processingTimeMs, 
                                           String resultFilePath, List<String> validationErrors) {
        log.info("Updating upload history for batch: {}", batchId);
        
        UploadHistory uploadHistory = uploadHistoryRepository.findByBatchId(batchId)
                .orElseThrow(() -> new RuntimeException("Upload history not found for batch: " + batchId));
        
        // Determine status based on results
        UploadHistory.UploadStatus status;
        if (errorRows == 0) {
            status = UploadHistory.UploadStatus.COMPLETED;
        } else if (successRows > 0) {
            status = UploadHistory.UploadStatus.PARTIAL;
        } else {
            status = UploadHistory.UploadStatus.FAILED;
        }
        
        uploadHistory.setStatus(status);
        uploadHistory.setTotalRows(totalRows);
        uploadHistory.setProcessedRows(processedRows);
        uploadHistory.setSuccessRows(successRows);
        uploadHistory.setErrorRows(errorRows);
        uploadHistory.setProcessingTimeMs(processingTimeMs);
        uploadHistory.setResultFilePath(resultFilePath);
        uploadHistory.setValidationErrors(validationErrors);
        uploadHistory.setUpdatedAt(Instant.now());
        
        return uploadHistoryRepository.save(uploadHistory);
    }

    /**
     * Update upload history with file paths
     */
    public UploadHistory updateFilePaths(String batchId, String uploadFilePath, String resultFilePath) {
        log.info("Updating file paths for batch: {}", batchId);
        
        UploadHistory uploadHistory = uploadHistoryRepository.findByBatchId(batchId)
                .orElseThrow(() -> new RuntimeException("Upload history not found for batch: " + batchId));
        
        uploadHistory.setUploadFilePath(uploadFilePath);
        uploadHistory.setResultFilePath(resultFilePath);
        uploadHistory.setUpdatedAt(Instant.now());
        
        return uploadHistoryRepository.save(uploadHistory);
    }

    /**
     * Mark upload as failed
     */
    public UploadHistory markUploadFailed(String batchId, String errorMessage) {
        log.error("Marking upload as failed for batch: {}, error: {}", batchId, errorMessage);
        
        UploadHistory uploadHistory = uploadHistoryRepository.findByBatchId(batchId)
                .orElseThrow(() -> new RuntimeException("Upload history not found for batch: " + batchId));
        
        uploadHistory.setStatus(UploadHistory.UploadStatus.FAILED);
        uploadHistory.setErrorMessage(errorMessage);
        uploadHistory.setUpdatedAt(Instant.now());
        
        return uploadHistoryRepository.save(uploadHistory);
    }

    /**
     * Get upload history by client ID
     */
    public List<UploadHistory> getUploadHistoryByClient(String clientId) {
        log.debug("Getting upload history for client: {}", clientId);
        return uploadHistoryRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    /**
     * Get upload history by client ID with pagination
     */
    public Page<UploadHistory> getUploadHistoryByClient(String clientId, Pageable pageable) {
        log.debug("Getting paginated upload history for client: {}", clientId);
        return uploadHistoryRepository.findByClientIdOrderByCreatedAtDesc(clientId, pageable);
    }

    /**
     * Get upload history by batch ID
     */
    public Optional<UploadHistory> getUploadHistoryByBatch(String batchId) {
        log.debug("Getting upload history for batch: {}", batchId);
        return uploadHistoryRepository.findByBatchId(batchId);
    }

    /**
     * Get upload history by status
     */
    public List<UploadHistory> getUploadHistoryByStatus(UploadHistory.UploadStatus status) {
        log.debug("Getting upload history by status: {}", status);
        return uploadHistoryRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * Get upload history by client and status
     */
    public List<UploadHistory> getUploadHistoryByClientAndStatus(String clientId, UploadHistory.UploadStatus status) {
        log.debug("Getting upload history for client: {} and status: {}", clientId, status);
        return uploadHistoryRepository.findByClientIdAndStatusOrderByCreatedAtDesc(clientId, status);
    }

    /**
     * Get recent uploads
     */
    public List<UploadHistory> getRecentUploads(String clientId, int days) {
        log.debug("Getting recent uploads for client: {} (last {} days)", clientId, days);
        Instant since = Instant.now().minusSeconds(days * 24 * 60 * 60);
        return uploadHistoryRepository.findRecentUploads(clientId, since);
    }

    /**
     * Search uploads by filename
     */
    public List<UploadHistory> searchUploadsByFilename(String clientId, String filenamePattern) {
        log.debug("Searching uploads by filename for client: {}, pattern: {}", clientId, filenamePattern);
        return uploadHistoryRepository.findByClientIdAndOriginalFileNameContaining(clientId, filenamePattern);
    }

    /**
     * Get upload statistics for client
     */
    public UploadStatistics getUploadStatistics(String clientId) {
        log.debug("Getting upload statistics for client: {}", clientId);
        
        List<UploadHistory> uploads = uploadHistoryRepository.findUploadStatisticsByClient(clientId);
        
        int totalUploads = uploads.size();
        int successfulUploads = (int) uploads.stream()
                .filter(u -> u.getStatus() == UploadHistory.UploadStatus.COMPLETED || 
                           u.getStatus() == UploadHistory.UploadStatus.PARTIAL)
                .count();
        int failedUploads = (int) uploads.stream()
                .filter(u -> u.getStatus() == UploadHistory.UploadStatus.FAILED)
                .count();
        
        int totalRows = uploads.stream().mapToInt(UploadHistory::getTotalRows).sum();
        int successRows = uploads.stream().mapToInt(UploadHistory::getSuccessRows).sum();
        int errorRows = uploads.stream().mapToInt(UploadHistory::getErrorRows).sum();
        
        long totalProcessingTime = uploads.stream().mapToLong(UploadHistory::getProcessingTimeMs).sum();
        
        return UploadStatistics.builder()
                .totalUploads(totalUploads)
                .successfulUploads(successfulUploads)
                .failedUploads(failedUploads)
                .totalRows(totalRows)
                .successRows(successRows)
                .errorRows(errorRows)
                .totalProcessingTimeMs(totalProcessingTime)
                .averageProcessingTimeMs(totalUploads > 0 ? totalProcessingTime / totalUploads : 0)
                .build();
    }

    /**
     * Clean up expired files
     */
    public int cleanupExpiredFiles() {
        log.info("Starting cleanup of expired upload history and files");
        
        // Get expired uploads
        List<UploadHistory> expiredUploads = uploadHistoryRepository.findExpiredUploads(Instant.now());
        
        int deletedFiles = 0;
        for (UploadHistory upload : expiredUploads) {
            try {
                // Delete files
                if (upload.getUploadFilePath() != null && fileStorageService.deleteFile(upload.getUploadFilePath())) {
                    deletedFiles++;
                }
                if (upload.getResultFilePath() != null && fileStorageService.deleteFile(upload.getResultFilePath())) {
                    deletedFiles++;
                }
                
                // Mark as deleted
                upload.setFilesDeleted(true);
                uploadHistoryRepository.save(upload);
                
            } catch (Exception e) {
                log.error("Error cleaning up files for upload: {}", upload.getId(), e);
            }
        }
        
        log.info("Cleanup completed. Deleted {} files for {} uploads", deletedFiles, expiredUploads.size());
        return deletedFiles;
    }

    /**
     * Upload statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UploadStatistics {
        private int totalUploads;
        private int successfulUploads;
        private int failedUploads;
        private int totalRows;
        private int successRows;
        private int errorRows;
        private long totalProcessingTimeMs;
        private long averageProcessingTimeMs;
    }
}
