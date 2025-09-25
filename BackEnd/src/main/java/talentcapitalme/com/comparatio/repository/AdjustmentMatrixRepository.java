package talentcapitalme.com.comparatio.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import talentcapitalme.com.comparatio.entity.AdjustmentMatrix;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AdjustmentMatrixRepository extends MongoRepository<AdjustmentMatrix, String> {

    /** Find the active adjustment matrix cell for the given performance bucket, compa ratio, and as-of date.
     *
     * @param perfBucket The performance bucket (1-5).
     * @param compa The compa ratio.
     * @param asOf The as-of date to check effectiveness.
     * @return An Optional containing the matching AdjustmentMatrix if found, otherwise empty.
     */
    @Query("{ 'perfBucket': ?0, 'active': true, " +
            " 'compaFrom': { $lte: ?1 }, 'compaTo': { $gt: ?1 }, " +
            " $and: [ { $or: [ { 'effectiveFrom': null }, { 'effectiveFrom': { $lte: ?2 } } ] }, " +
            " { $or: [ { 'effectiveTo': null }, { 'effectiveTo': { $gte: ?2 } } ] } ] }")
    Optional<AdjustmentMatrix> findActiveCell(int perfBucket, BigDecimal compa, LocalDate asOf);

    /**
     * Find all adjustment matrices for a specific client.
     *
     * @param clientId The client ID to filter by.
     * @return List of adjustment matrices for the client.
     */
    List<AdjustmentMatrix> findByClientId(String clientId);
    
    /**
     * Find all active adjustment matrices for a specific client.
     *
     * @param clientId The client ID to filter by.
     * @return List of active adjustment matrices for the client.
     */
    List<AdjustmentMatrix> findByClientIdAndActiveTrue(String clientId);
    
    /**
     * Find the active adjustment matrix cell for a specific client.
     * Each client has their own separate set of matrices.
     * 
     * @param perfBucket The performance bucket (1-3).
     * @param compa The compa ratio.
     * @param asOf The as-of date to check effectiveness.  
     * @param clientId The client ID - required for client-specific calculations.
     * @return An Optional containing the matching AdjustmentMatrix if found, otherwise empty.
     */
    @Query("{ 'clientId': ?3, 'perfBucket': ?0, 'active': true, " +
           "'compaFrom': { $lte: ?1 }, 'compaTo': { $gt: ?1 }, " +
           "$and: [ { $or: [ { 'effectiveFrom': null }, { 'effectiveFrom': { $lte: ?2 } } ] }, " +
           "        { $or: [ { 'effectiveTo': null }, { 'effectiveTo': { $gte: ?2 } } ] } ] }")
    Optional<AdjustmentMatrix> findClientActiveCell(int perfBucket, BigDecimal compa, LocalDate asOf, String clientId);
    
    /**
     * Check if matrices exist for a specific client.
     * 
     * @param clientId The client ID to check.
     * @return True if matrices exist for the client, false otherwise.
     */
    boolean existsByClientId(String clientId);
    
    /**
     * Delete all matrices for a specific client.
     * Used when a client is removed from the system.
     * 
     * @param clientId The client ID.
     */
    void deleteByClientId(String clientId);
    
    /**
     * Count matrices for a specific client.
     * 
     * @param clientId The client ID.
     * @return Number of matrices for the client.
     */
    long countByClientId(String clientId);
}

