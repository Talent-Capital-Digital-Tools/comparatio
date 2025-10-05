package talentcapitalme.com.comparatio.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Utility class for file operations and validation
 * Centralizes file-related utility methods to avoid code duplication
 */
@Slf4j
public class FileUtils {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
    
    // File size limits
    public static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    public static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    
    // Allowed file extensions
    public static final String[] ALLOWED_EXCEL_EXTENSIONS = {".xlsx", ".xls"};
    public static final String[] ALLOWED_IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};

    /**
     * Generate unique filename with timestamp
     */
    public static String generateUniqueFilename(String originalFilename, String prefix) {
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        return String.format("%s_%s_%s%s", prefix, uuid, timestamp, extension);
    }

    /**
     * Generate profile image filename with user ID and timestamp
     */
    public static String generateProfileImageFilename(String userId, String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        
        return String.format("profile_image_%s_%s%s", userId, timestamp, extension);
    }

    /**
     * Get file extension from filename
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        
        return filename.substring(lastDotIndex).toLowerCase();
    }

    /**
     * Validate Excel file
     */
    public static boolean isValidExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }
        
        String extension = getFileExtension(filename);
        for (String allowedExt : ALLOWED_EXCEL_EXTENSIONS) {
            if (extension.equals(allowedExt)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Validate image file
     */
    public static boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }
        
        String extension = getFileExtension(filename);
        for (String allowedExt : ALLOWED_IMAGE_EXTENSIONS) {
            if (extension.equals(allowedExt)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Validate file size
     */
    public static boolean isValidFileSize(MultipartFile file, long maxSize) {
        return file != null && file.getSize() <= maxSize;
    }

    /**
     * Format file size in human readable format
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Sanitize filename to prevent directory traversal attacks
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null) {
            return "";
        }
        
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_")
                      .replaceAll("\\.{2,}", ".")
                      .replaceAll("^[._-]+", "")
                      .replaceAll("[._-]+$", "");
    }

    /**
     * Get content type based on file extension
     */
    public static String getContentType(String filename) {
        String extension = getFileExtension(filename);
        
        switch (extension) {
            case ".xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case ".xls":
                return "application/vnd.ms-excel";
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            case ".bmp":
                return "image/bmp";
            default:
                return "application/octet-stream";
        }
    }
}
