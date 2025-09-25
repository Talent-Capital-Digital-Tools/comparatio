package talentcapitalme.com.comparatio.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import talentcapitalme.com.comparatio.entity.CalculationResult;

import java.util.List;

public interface CalculationResultRepository extends MongoRepository<CalculationResult, String> {
    List<CalculationResult> findByBatchId(String batchId);
    List<CalculationResult> findByClientIdAndBatchId(String clientId, String batchId);
    long countByClientId(String clientId);
}