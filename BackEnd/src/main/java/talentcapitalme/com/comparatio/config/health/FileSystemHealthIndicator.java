package talentcapitalme.com.comparatio.config.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import talentcapitalme.com.comparatio.config.AppProperties;

import java.io.File;

/**
 * File system health indicator for monitoring file storage
 * Checks if file upload directories are accessible and writable
 * 
 * Note: This class is temporarily disabled until actuator dependencies are properly resolved
 * Uncomment the HealthIndicator implementation once actuator is working
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileSystemHealthIndicator /* implements HealthIndicator */ {

    private final AppProperties appProperties;

    // @Override
    public Object health() {
        try {
            // Check upload directory
            File uploadDir = new File(appProperties.getFileUpload().getUploadPath());
            boolean uploadDirExists = uploadDir.exists();
            boolean uploadDirWritable = uploadDir.canWrite();
            
            // Check profile images directory
            File profileDir = new File(appProperties.getProfileImagesFullPath());
            boolean profileDirExists = profileDir.exists();
            boolean profileDirWritable = profileDir.canWrite();
            
            // Check client files directory
            File clientDir = new File(appProperties.getClientFilesFullPath());
            boolean clientDirExists = clientDir.exists();
            boolean clientDirWritable = clientDir.canWrite();
            
            boolean allHealthy = uploadDirExists && uploadDirWritable && 
                               profileDirExists && profileDirWritable &&
                               clientDirExists && clientDirWritable;
            
            if (allHealthy) {
                log.info("File system health check passed - All directories accessible");
                return "UP";
            } else {
                log.warn("File system health check failed - Some directories not accessible");
                return "DOWN";
            }
                    
        } catch (Exception e) {
            log.error("File system health check failed", e);
            return "DOWN";
        }
    }
}
