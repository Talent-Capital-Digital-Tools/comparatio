package talentcapitalme.com.comparatio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import talentcapitalme.com.comparatio.entity.UploadHistory;
import talentcapitalme.com.comparatio.security.Authz;
import talentcapitalme.com.comparatio.service.IFileStorageService;
import talentcapitalme.com.comparatio.service.IUploadHistoryService;
import java.util.List;
import java.util.Optional;

/**
 * Upload History Management Controller
 * 
 * Purpose: Handles historical tracking and file management for Excel uploads
 * - Upload history tracking and retrieval
 * - Original and result file downloads
 * - Upload statistics and analytics
 * - File cleanup and maintenance
 * - Client-specific upload history access
 */
@Slf4j
@RestController
@RequestMapping("/api/upload-history")
@RequiredArgsConstructor
@Tag(name = "Upload History", description = "Historical tracking and file management for Excel uploads")
public class UploadHistoryController {

    private final IUploadHistoryService uploadHistoryService;
    private final IFileStorageService fileStorageService;

    @Operation(summary = "Get Upload History", description = "Retrieve upload history for current client")
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<List<UploadHistory>> getUploadHistory() {
        log.info("Getting upload history for current client");
        
        try {
            // Get client ID from security context
            String clientId = getCurrentClientId();
            List<UploadHistory> history = uploadHistoryService.getUploadHistoryByClient(clientId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting upload history", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get Paginated Upload History", description = "Retrieve paginated upload history for current client")
    @GetMapping("/paginated")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<Page<UploadHistory>> getUploadHistoryPaginated(Pageable pageable) {
        log.info("Getting paginated upload history for current client");
        
        try {
            String clientId = getCurrentClientId();
            Page<UploadHistory> history = uploadHistoryService.getUploadHistoryByClient(clientId, pageable);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting paginated upload history", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get Upload History by Batch", description = "Retrieve upload history for a specific batch ID")
    @GetMapping("/batch/{batchId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<UploadHistory> getUploadHistoryByBatch(@Parameter(description = "Batch ID") @PathVariable String batchId) {
        log.info("Getting upload history for batch: {}", batchId);
        
        try {
            Optional<UploadHistory> history = uploadHistoryService.getUploadHistoryByBatch(batchId);
            if (history.isPresent()) {
                // Verify client access
                String clientId = getCurrentClientId();
                if (!history.get().getClientId().equals(clientId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                return ResponseEntity.ok(history.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting upload history by batch", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Download Original File", description = "Download the original Excel file that was uploaded for a specific batch")
    @GetMapping("/batch/{batchId}/download/original")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<Resource> downloadOriginalFile(@Parameter(description = "Batch ID") @PathVariable String batchId) {
        log.info("Downloading original file for batch: {}", batchId);
        
        try {
            Optional<UploadHistory> history = uploadHistoryService.getUploadHistoryByBatch(batchId);
            if (history.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            UploadHistory upload = history.get();
            String clientId = getCurrentClientId();
            if (!upload.getClientId().equals(clientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            if (upload.getUploadFilePath() == null || !fileStorageService.fileExists(upload.getUploadFilePath())) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = fileStorageService.loadFileAsResource(upload.getUploadFilePath());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + upload.getOriginalFileName() + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error downloading original file", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Download Result File", description = "Download the processed Excel file with calculation results for a specific batch")
    @GetMapping("/batch/{batchId}/download/result")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<Resource> downloadResultFile(@Parameter(description = "Batch ID") @PathVariable String batchId) {
        log.info("Downloading result file for batch: {}", batchId);
        
        try {
            Optional<UploadHistory> history = uploadHistoryService.getUploadHistoryByBatch(batchId);
            if (history.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            UploadHistory upload = history.get();
            String clientId = getCurrentClientId();
            if (!upload.getClientId().equals(clientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            if (upload.getResultFilePath() == null || !fileStorageService.fileExists(upload.getResultFilePath())) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = fileStorageService.loadFileAsResource(upload.getResultFilePath());
            
            String resultFileName = "result_" + upload.getOriginalFileName();
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + resultFileName + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error downloading result file", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get Upload Statistics", description = "Retrieve upload statistics and analytics for current client")
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<IUploadHistoryService.UploadStatistics> getUploadStatistics() {
        log.info("Getting upload statistics for current client");
        
        try {
            String clientId = getCurrentClientId();
            IUploadHistoryService.UploadStatistics stats = uploadHistoryService.getUploadStatistics(clientId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting upload statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Search Uploads by Filename", description = "Search upload history by filename pattern")
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<List<UploadHistory>> searchUploads(@Parameter(description = "Filename pattern to search") @RequestParam String filename) {
        log.info("Searching uploads by filename: {}", filename);
        
        try {
            String clientId = getCurrentClientId();
            List<UploadHistory> results = uploadHistoryService.searchUploadsByFilename(clientId, filename);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error searching uploads", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get Recent Uploads", description = "Retrieve recent uploads within specified number of days")
    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<List<UploadHistory>> getRecentUploads(@Parameter(description = "Number of days to look back") @RequestParam(defaultValue = "7") int days) {
        log.info("Getting recent uploads for last {} days", days);
        
        try {
            String clientId = getCurrentClientId();
            List<UploadHistory> recent = uploadHistoryService.getRecentUploads(clientId, days);
            return ResponseEntity.ok(recent);
        } catch (Exception e) {
            log.error("Error getting recent uploads", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Cleanup Expired Files", description = "Clean up expired upload files and history (Super Admin only)")
    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> cleanupExpiredFiles() {
        log.info("Starting cleanup of expired files");
        
        try {
            int deletedFiles = uploadHistoryService.cleanupExpiredFiles();
            return ResponseEntity.ok("Cleanup completed. Deleted " + deletedFiles + " files.");
        } catch (Exception e) {
            log.error("Error during cleanup", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get current client ID from security context
     */
    private String getCurrentClientId() {
        return Authz.getCurrentUserClientId();
    }
}
