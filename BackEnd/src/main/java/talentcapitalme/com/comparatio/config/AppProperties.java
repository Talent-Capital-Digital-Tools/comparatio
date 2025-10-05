package talentcapitalme.com.comparatio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Application configuration properties
 * Centralizes all application configuration in one place for better maintainability
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * JWT configuration
     */
    private Jwt jwt = new Jwt();

    /**
     * File upload configuration
     */
    private FileUpload fileUpload = new FileUpload();

    /**
     * Database configuration
     */
    private Database database = new Database();

    /**
     * Security configuration
     */
    private Security security = new Security();

    /**
     * Monitoring configuration
     */
    private Monitoring monitoring = new Monitoring();

    @Data
    public static class Jwt {
        private String secret = "mySecretKey";
        private long expiration = 86400000; // 24 hours in milliseconds
        private long refreshExpiration = 604800000; // 7 days in milliseconds
        private String issuer = "Comparatio";
        private String audience = "Comparatio-Users";
    }

    @Data
    public static class FileUpload {
        private String uploadPath = "uploads";
        private String profileImagesPath = "uploads/profiles";
        private String clientFilesPath = "uploads/clients";
        private long maxFileSize = 52428800; // 50MB
        private long maxImageSize = 5242880; // 5MB
        private String[] allowedExcelExtensions = {".xlsx", ".xls"};
        private String[] allowedImageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
    }

    @Data
    public static class Database {
        private int connectionTimeout = 30000; // 30 seconds
        private int socketTimeout = 30000; // 30 seconds
        private int maxPoolSize = 100;
        private int minPoolSize = 5;
        private int maxIdleTime = 60000; // 1 minute
    }

    @Data
    public static class Security {
        private boolean enableCors = true;
        private String[] corsAllowedOrigins = {"*"};
        private String[] corsAllowedMethods = {"GET", "POST", "PUT", "DELETE", "OPTIONS"};
        private String[] corsAllowedHeaders = {"*"};
        private boolean enableCsrf = false;
        private int maxLoginAttempts = 5;
        private int lockoutDuration = 300; // 5 minutes
    }

    @Data
    public static class Monitoring {
        private boolean enableMetrics = true;
        private boolean enableHealthChecks = true;
        private boolean enableRequestLogging = true;
        private long slowQueryThreshold = 1000; // 1 second
        private int maxLogEntries = 10000;
    }

    /**
     * Get full file path for profile images
     */
    public String getProfileImagesFullPath() {
        return fileUpload.uploadPath + "/" + fileUpload.profileImagesPath;
    }

    /**
     * Get full file path for client files
     */
    public String getClientFilesFullPath() {
        return fileUpload.uploadPath + "/" + fileUpload.clientFilesPath;
    }

    /**
     * Check if file size is within limits
     */
    public boolean isValidFileSize(long fileSize, boolean isImage) {
        long maxSize = isImage ? fileUpload.maxImageSize : fileUpload.maxFileSize;
        return fileSize <= maxSize;
    }

    /**
     * Get JWT secret for signing tokens
     */
    public String getJwtSecret() {
        return jwt.secret;
    }

    /**
     * Get JWT expiration time
     */
    public long getJwtExpiration() {
        return jwt.expiration;
    }
}
