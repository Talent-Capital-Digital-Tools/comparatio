package talentcapitalme.com.comparatio.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import talentcapitalme.com.comparatio.util.FileUtils;

/**
 * Custom validator for file upload operations
 * Validates file types, sizes, and security constraints
 */
@Slf4j
@Component
public class FileUploadValidator {

    /**
     * Validate Excel file upload
     */
    public void validateExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Excel file is required");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("File must have a valid name");
        }
        
        // Validate file type
        if (!FileUtils.isValidExcelFile(file)) {
            throw new IllegalArgumentException(
                String.format("Invalid file type. Only %s files are supported", 
                    String.join(", ", FileUtils.ALLOWED_EXCEL_EXTENSIONS)));
        }
        
        // Validate file size
        if (!FileUtils.isValidFileSize(file, FileUtils.MAX_FILE_SIZE)) {
            throw new IllegalArgumentException(
                String.format("File size exceeds limit. Maximum size is %s", 
                    FileUtils.formatFileSize(FileUtils.MAX_FILE_SIZE)));
        }
        
        // Sanitize filename
        String sanitizedFilename = FileUtils.sanitizeFilename(filename);
        if (!sanitizedFilename.equals(filename)) {
            log.warn("Filename sanitized from '{}' to '{}'", filename, sanitizedFilename);
        }
    }

    /**
     * Validate image file upload
     */
    public void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("File must have a valid name");
        }
        
        // Validate file type
        if (!FileUtils.isValidImageFile(file)) {
            throw new IllegalArgumentException(
                String.format("Invalid image type. Only %s files are supported", 
                    String.join(", ", FileUtils.ALLOWED_IMAGE_EXTENSIONS)));
        }
        
        // Validate file size
        if (!FileUtils.isValidFileSize(file, FileUtils.MAX_IMAGE_SIZE)) {
            throw new IllegalArgumentException(
                String.format("Image size exceeds limit. Maximum size is %s", 
                    FileUtils.formatFileSize(FileUtils.MAX_IMAGE_SIZE)));
        }
        
        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be a valid image");
        }
        
        // Sanitize filename
        String sanitizedFilename = FileUtils.sanitizeFilename(filename);
        if (!sanitizedFilename.equals(filename)) {
            log.warn("Image filename sanitized from '{}' to '{}'", filename, sanitizedFilename);
        }
    }

    /**
     * Validate file security
     */
    public void validateFileSecurity(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return;
        }
        
        // Check for directory traversal attempts
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new SecurityException("Invalid filename: potential directory traversal attempt");
        }
        
        // Check for suspicious file extensions
        String extension = FileUtils.getFileExtension(filename);
        String[] suspiciousExtensions = {".exe", ".bat", ".cmd", ".com", ".scr", ".pif", ".vbs", ".js"};
        
        for (String suspiciousExt : suspiciousExtensions) {
            if (extension.equalsIgnoreCase(suspiciousExt)) {
                throw new SecurityException("File type not allowed for security reasons");
            }
        }
        
        // Check for extremely long filenames
        if (filename.length() > 255) {
            throw new IllegalArgumentException("Filename too long (maximum 255 characters)");
        }
    }
}
