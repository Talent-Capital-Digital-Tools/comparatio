package talentcapitalme.com.comparatio.service;

import talentcapitalme.com.comparatio.entity.AdjustmentMatrix;

import java.util.List;

/**
 * Interface for Matrix Management Service operations
 */
public interface IMatrixManagementService {
    
    /**
     * Get all matrices for a specific client with comprehensive validation
     */
    List<AdjustmentMatrix> getClientMatrices(String clientId);
    
    /**
     * Get a specific matrix by ID with client validation
     */
    AdjustmentMatrix getMatrixById(String matrixId, String clientId);
    
    /**
     * Create a new matrix with comprehensive validation
     */
    AdjustmentMatrix createMatrix(String clientId, AdjustmentMatrix matrix);
    
    /**
     * Update an existing matrix with comprehensive validation
     */
    AdjustmentMatrix updateMatrix(String matrixId, String clientId, AdjustmentMatrix matrixUpdate);
    
    /**
     * Delete a matrix with proper validation
     */
    void deleteMatrix(String matrixId, String clientId);
    
    /**
     * Bulk update matrices for a client
     */
    List<AdjustmentMatrix> bulkUpdateMatrices(String clientId, List<AdjustmentMatrix> matrices);
    
    /**
     * Reset matrices to default for a client
     */
    List<AdjustmentMatrix> resetToDefaultMatrices(String clientId);
}
