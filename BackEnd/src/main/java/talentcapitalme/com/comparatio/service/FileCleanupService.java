package talentcapitalme.com.comparatio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service for scheduled file cleanup tasks
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileCleanupService {

    private final UploadHistoryService uploadHistoryService;
    private final FileStorageService fileStorageService;

    /**
     * Clean up expired files every day at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredFiles() {
        log.info("Starting scheduled cleanup of expired files");
        
        try {
            // Clean up upload history and associated files
            int deletedFiles = uploadHistoryService.cleanupExpiredFiles();
            
            // Clean up any orphaned files
            int orphanedFiles = fileStorageService.cleanupExpiredFiles();
            
            log.info("Scheduled cleanup completed. Deleted {} files from history and {} orphaned files", 
                    deletedFiles, orphanedFiles);
                    
        } catch (Exception e) {
            log.error("Error during scheduled file cleanup", e);
        }
    }

    /**
     * Clean up files older than 7 days every 6 hours
     */
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 6 hours
    public void cleanupOldFiles() {
        log.info("Starting cleanup of old files");
        
        try {
            int deletedFiles = fileStorageService.cleanupExpiredFiles();
            log.info("Old files cleanup completed. Deleted {} files", deletedFiles);
        } catch (Exception e) {
            log.error("Error during old files cleanup", e);
        }
    }
}
