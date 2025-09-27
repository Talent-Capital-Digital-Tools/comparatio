package talentcapitalme.com.comparatio.service;

import talentcapitalme.com.comparatio.dto.MatrixValidationResult;
import talentcapitalme.com.comparatio.entity.AdjustmentMatrix;

import java.util.List;

/**
 * Interface for Matrix Validation Service operations
 */
public interface IMatrixValidationService {
    
    /**
     * Comprehensive validation of matrix configuration
     */
    MatrixValidationResult validateMatrixConfiguration(List<AdjustmentMatrix> matrices);
    
}
