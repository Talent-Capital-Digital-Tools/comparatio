package talentcapitalme.com.comparatio.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import talentcapitalme.com.comparatio.entity.UploadHistory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for UploadHistory operations
 */
public interface UploadHistoryRepository extends MongoRepository<UploadHistory, String> {
    
    /**
     * Find upload history by client ID
     */
    List<UploadHistory> findByClientIdOrderByCreatedAtDesc(String clientId);
    
    /**
     * Find upload history by client ID with pagination
     */
    Page<UploadHistory> findByClientIdOrderByCreatedAtDesc(String clientId, Pageable pageable);
    
    /**
     * Find upload history by batch ID
     */
    Optional<UploadHistory> findByBatchId(String batchId);
    
    /**
     * Find upload history by status
     */
    List<UploadHistory> findByStatusOrderByCreatedAtDesc(UploadHistory.UploadStatus status);
    
    /**
     * Find upload history by client ID and status
     */
    List<UploadHistory> findByClientIdAndStatusOrderByCreatedAtDesc(String clientId, UploadHistory.UploadStatus status);
    
    /**
     * Find upload history by uploaded by user
     */
    List<UploadHistory> findByUploadedByOrderByCreatedAtDesc(String uploadedBy);
    
    /**
     * Find upload history within date range
     */
    @Query("{ 'clientId': ?0, 'createdAt': { $gte: ?1, $lte: ?2 } }")
    List<UploadHistory> findByClientIdAndCreatedAtBetween(String clientId, Instant startDate, Instant endDate);
    
    /**
     * Find upload history by file name pattern
     */
    @Query("{ 'clientId': ?0, 'originalFileName': { $regex: ?1, $options: 'i' } }")
    List<UploadHistory> findByClientIdAndOriginalFileNameContaining(String clientId, String fileNamePattern);
    
    /**
     * Find expired uploads (for cleanup)
     */
    @Query("{ 'expiresAt': { $lt: ?0 }, 'filesDeleted': false }")
    List<UploadHistory> findExpiredUploads(Instant currentTime);
    
    /**
     * Find uploads by tags
     */
    @Query("{ 'clientId': ?0, 'tags': { $regex: ?1, $options: 'i' } }")
    List<UploadHistory> findByClientIdAndTagsContaining(String clientId, String tag);
    
    /**
     * Count uploads by client and status
     */
    long countByClientIdAndStatus(String clientId, UploadHistory.UploadStatus status);
    
    /**
     * Count total uploads by client
     */
    long countByClientId(String clientId);
    
    /**
     * Find recent uploads (last N days)
     */
    @Query("{ 'clientId': ?0, 'createdAt': { $gte: ?1 } }")
    List<UploadHistory> findRecentUploads(String clientId, Instant since);
    
    /**
     * Find uploads with files that need cleanup
     */
    @Query("{ 'filesDeleted': false, 'expiresAt': { $lt: ?0 } }")
    List<UploadHistory> findUploadsForCleanup(Instant currentTime);
    
    /**
     * Find successful uploads by client
     */
    @Query("{ 'clientId': ?0, 'status': { $in: ['COMPLETED', 'PARTIAL'] } }")
    List<UploadHistory> findSuccessfulUploadsByClient(String clientId);
    
    /**
     * Get upload statistics by client
     */
    @Query(value = "{ 'clientId': ?0 }", fields = "{ 'status': 1, 'totalRows': 1, 'successRows': 1, 'errorRows': 1, 'processingTimeMs': 1 }")
    List<UploadHistory> findUploadStatisticsByClient(String clientId);
}
