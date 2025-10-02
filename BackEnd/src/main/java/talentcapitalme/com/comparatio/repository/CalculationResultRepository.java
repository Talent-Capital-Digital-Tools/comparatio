package talentcapitalme.com.comparatio.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import talentcapitalme.com.comparatio.entity.CalculationResult;

import java.util.List;

public interface CalculationResultRepository extends MongoRepository<CalculationResult, String> {
    List<CalculationResult> findByBatchId(String batchId);

    List<CalculationResult> findByClientIdAndBatchId(String clientId, String batchId);

    // Find bulk calculation results only (exclude individual calculations)
    List<CalculationResult> findByClientIdAndBatchIdAndBatchIdNotLike(String clientId, String batchId, String excludePattern);

    long countByClientId(String clientId);

    // Pageable queries for efficient database pagination
    Page<CalculationResult> findByClientId(String clientId, Pageable pageable);

    Page<CalculationResult> findByClientIdAndBatchId(String clientId, String batchId, Pageable pageable);
}