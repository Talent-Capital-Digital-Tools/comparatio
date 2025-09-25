package talentcapitalme.com.comparatio.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * Entity to track Excel upload history and results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "upload_history")
public class UploadHistory {
    
    @Id
    private String id;
    
    // Client information
    private String clientId;
    private String clientName;
    
    // Upload information
    private String originalFileName;
    private String uploadedFileName; // Stored file name
    private String resultFileName;   // Generated result file name
    private long fileSizeBytes;
    private String contentType;
    
    // Processing information
    private String batchId;
    private int totalRows;
    private int processedRows;
    private int successRows;
    private int errorRows;
    private long processingTimeMs;
    
    // File storage paths
    private String uploadFilePath;    // Path to original uploaded file
    private String resultFilePath;    // Path to generated result file
    
    // Status and results
    private UploadStatus status;
    private String errorMessage;
    private List<String> validationErrors;
    
    // User information
    private String uploadedBy;
    private String uploadedByEmail;
    
    // Timestamps
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
    
    // File retention
    private Instant expiresAt; // When files should be cleaned up
    private boolean filesDeleted;
    
    // Additional metadata
    private String description;
    private String tags; // Comma-separated tags for categorization
    
    /**
     * Upload status enumeration
     */
    public enum UploadStatus {
        UPLOADING,      // File is being uploaded
        PROCESSING,     // File is being processed
        COMPLETED,      // Processing completed successfully
        FAILED,         // Processing failed
        PARTIAL,        // Some rows processed successfully
        CANCELLED       // Processing was cancelled
    }
    
    /**
     * Check if the upload is successful
     */
    public boolean isSuccessful() {
        return status == UploadStatus.COMPLETED || status == UploadStatus.PARTIAL;
    }
    
    /**
     * Check if files are still available
     */
    public boolean hasFiles() {
        return !filesDeleted && uploadFilePath != null && resultFilePath != null;
    }
    
    /**
     * Check if files have expired
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
}
