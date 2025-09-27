package talentcapitalme.com.comparatio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import talentcapitalme.com.comparatio.entity.AdjustmentMatrix;
import talentcapitalme.com.comparatio.entity.User;
import talentcapitalme.com.comparatio.enumeration.UserRole;
import talentcapitalme.com.comparatio.exception.NotFoundException;
import talentcapitalme.com.comparatio.exception.ValidationException;
import talentcapitalme.com.comparatio.repository.AdjustmentMatrixRepository;
import talentcapitalme.com.comparatio.repository.UserRepository;
import talentcapitalme.com.comparatio.security.Authz;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatrixManagementService implements IMatrixManagementService {

    private final AdjustmentMatrixRepository matrixRepo;
    private final UserRepository userRepo;
    private final MatrixSeederService seederService;

    /**
     * Get all matrices for a specific client with comprehensive validation
     */
    public List<AdjustmentMatrix> getClientMatrices(String clientId) {
        validateClientAccess(clientId);
        log.info("Retrieving matrices for client: {}", clientId);
        return matrixRepo.findByClientIdAndActiveTrue(clientId);
    }

    /**
     * Get a specific matrix by ID with client validation
     */
    public AdjustmentMatrix getMatrixById(String matrixId, String clientId) {
        validateClientAccess(clientId);
        
        AdjustmentMatrix matrix = matrixRepo.findById(matrixId)
                .orElseThrow(() -> new NotFoundException("Matrix not found with ID: " + matrixId));
        
        if (!clientId.equals(matrix.getClientId())) {
            throw new ValidationException("Matrix does not belong to the specified client");
        }
        
        log.info("Retrieved matrix {} for client {}", matrixId, clientId);
        return matrix;
    }

    /**
     * Create a new matrix with comprehensive validation
     */
    @Transactional
    public AdjustmentMatrix createMatrix(String clientId, AdjustmentMatrix matrix) {
        validateClientAccess(clientId);
        validateMatrixData(matrix);
        
        // Set client ID and generate unique ID
        matrix.setClientId(clientId);
        matrix.setId(generateMatrixId(clientId, matrix));
        
        // Validate no overlapping ranges
        validateNoOverlappingRanges(clientId, matrix);
        
        AdjustmentMatrix saved = matrixRepo.save(matrix);
        log.info("Created matrix {} for client {}", saved.getId(), clientId);
        return saved;
    }

    /**
     * Update an existing matrix with comprehensive validation
     */
    @Transactional
    public AdjustmentMatrix updateMatrix(String matrixId, String clientId, AdjustmentMatrix matrixUpdate) {
        validateClientAccess(clientId);
        
        AdjustmentMatrix existing = getMatrixById(matrixId, clientId);
        
        // Validate updated data
        validateMatrixData(matrixUpdate);
        
        // Check for overlapping ranges (excluding current matrix)
        validateNoOverlappingRanges(clientId, matrixUpdate, matrixId);
        
        // Update fields
        existing.setPerfBucket(matrixUpdate.getPerfBucket());
        existing.setCompaFrom(matrixUpdate.getCompaFrom());
        existing.setCompaTo(matrixUpdate.getCompaTo());
        existing.setPctLt5Years(matrixUpdate.getPctLt5Years());
        existing.setPctGte5Years(matrixUpdate.getPctGte5Years());
        existing.setActive(matrixUpdate.getActive());
        
        // Update ID if compa range changed
        if (!existing.getCompaFrom().equals(matrixUpdate.getCompaFrom()) || 
            !existing.getCompaTo().equals(matrixUpdate.getCompaTo())) {
            existing.setId(generateMatrixId(clientId, existing));
        }
        
        AdjustmentMatrix saved = matrixRepo.save(existing);
        log.info("Updated matrix {} for client {}", saved.getId(), clientId);
        return saved;
    }

    /**
     * Delete a matrix with validation
     */
    @Transactional
    public void deleteMatrix(String matrixId, String clientId) {
        validateClientAccess(clientId);
        
        AdjustmentMatrix matrix = getMatrixById(matrixId, clientId);
        
        // Check if this is the last matrix for this performance bucket
        List<AdjustmentMatrix> remainingMatrices = matrixRepo.findByClientIdAndActiveTrue(clientId)
                .stream()
                .filter(m -> m.getPerfBucket().equals(matrix.getPerfBucket()) && !m.getId().equals(matrixId))
                .toList();
        
        if (remainingMatrices.isEmpty()) {
            throw new ValidationException("Cannot delete the last matrix for performance bucket " + 
                matrix.getPerfBucket() + ". At least one matrix must remain for each performance bucket.");
        }
        
        matrixRepo.deleteById(matrixId);
        log.info("Deleted matrix {} for client {}", matrixId, clientId);
    }

    /**
     * Bulk update matrices for a client
     */
    @Transactional
    public List<AdjustmentMatrix> bulkUpdateMatrices(String clientId, List<AdjustmentMatrix> matrices) {
        validateClientAccess(clientId);
        
        log.info("Starting bulk update of {} matrices for client {}", matrices.size(), clientId);
        
        // Validate all matrices
        for (AdjustmentMatrix matrix : matrices) {
            validateMatrixData(matrix);
            matrix.setClientId(clientId);
        }
        
        // Validate no overlapping ranges across all matrices
        validateNoOverlappingRangesBulk(clientId, matrices);
        
        // Delete existing matrices for this client
        matrixRepo.deleteByClientId(clientId);
        
        // Save new matrices
        List<AdjustmentMatrix> saved = matrixRepo.saveAll(matrices);
        log.info("Bulk updated {} matrices for client {}", saved.size(), clientId);
        
        return saved;
    }

    /**
     * Reset matrices to default for a client
     */
    @Transactional
    public List<AdjustmentMatrix> resetToDefaultMatrices(String clientId) {
        validateClientAccess(clientId);
        
        log.info("Resetting matrices to default for client {}", clientId);
        
        // Delete existing matrices
        matrixRepo.deleteByClientId(clientId);
        
        // Create default matrices
        seederService.seedDefaultsForClient(clientId);
        
        List<AdjustmentMatrix> defaultMatrices = matrixRepo.findByClientIdAndActiveTrue(clientId);
        log.info("Reset {} default matrices for client {}", defaultMatrices.size(), clientId);
        
        return defaultMatrices;
    }

    /**
     * Validate client access based on user role
     */
    private void validateClientAccess(String clientId) {
        UserRole currentRole = Authz.getCurrentUserRole();
        
        if (currentRole == UserRole.SUPER_ADMIN) {
            // Super admin can access any client
            if (clientId == null || clientId.trim().isEmpty()) {
                throw new ValidationException("Client ID is required");
            }
            
            // Verify client exists
            if (!userRepo.existsById(clientId)) {
                throw new NotFoundException("Client not found with ID: " + clientId);
            }
            
            User client = userRepo.findById(clientId).orElseThrow();
            if (client.getRole() != UserRole.CLIENT_ADMIN) {
                throw new ValidationException("Specified user is not a CLIENT_ADMIN");
            }
            
        } else if (currentRole == UserRole.CLIENT_ADMIN) {
            // Client admin can only access their own matrices
            String userClientId = Authz.getCurrentUserClientId();
            if (!clientId.equals(userClientId)) {
                throw new ValidationException("Access denied: You can only manage your own matrices");
            }
        } else {
            throw new ValidationException("Insufficient permissions to manage matrices");
        }
    }

    /**
     * Validate matrix data
     */
    private void validateMatrixData(AdjustmentMatrix matrix) {
        if (matrix == null) {
            throw new ValidationException("Matrix data cannot be null");
        }
        
        if (matrix.getPerfBucket() == null || matrix.getPerfBucket() < 1 || matrix.getPerfBucket() > 3) {
            throw new ValidationException("Performance bucket must be between 1 and 3");
        }
        
        if (matrix.getCompaFrom() == null || matrix.getCompaTo() == null) {
            throw new ValidationException("Compa range (from/to) is required");
        }
        
        if (matrix.getCompaFrom().compareTo(matrix.getCompaTo()) >= 0) {
            throw new ValidationException("Compa 'from' must be less than 'to'");
        }
        
        if (matrix.getCompaFrom().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Compa 'from' cannot be negative");
        }
        
        if (matrix.getPctLt5Years() == null || matrix.getPctGte5Years() == null) {
            throw new ValidationException("Percentage values for both experience levels are required");
        }
        
        if (matrix.getPctLt5Years().compareTo(BigDecimal.ZERO) < 0 || 
            matrix.getPctGte5Years().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Percentage values cannot be negative");
        }
        
        if (matrix.getPctLt5Years().compareTo(BigDecimal.valueOf(100)) > 0 || 
            matrix.getPctGte5Years().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ValidationException("Percentage values cannot exceed 100%");
        }
        
    }

    /**
     * Validate no overlapping ranges for a single matrix
     */
    private void validateNoOverlappingRanges(String clientId, AdjustmentMatrix newMatrix) {
        validateNoOverlappingRanges(clientId, newMatrix, null);
    }

    /**
     * Validate no overlapping ranges for a single matrix (excluding specified ID)
     */
    private void validateNoOverlappingRanges(String clientId, AdjustmentMatrix newMatrix, String excludeId) {
        List<AdjustmentMatrix> existingMatrices = matrixRepo.findByClientIdAndActiveTrue(clientId)
                .stream()
                .filter(m -> m.getPerfBucket().equals(newMatrix.getPerfBucket()))
                .filter(m -> excludeId == null || !m.getId().equals(excludeId))
                .toList();
        
        for (AdjustmentMatrix existing : existingMatrices) {
            if (rangesOverlap(newMatrix.getCompaFrom(), newMatrix.getCompaTo(),
                            existing.getCompaFrom(), existing.getCompaTo())) {
                throw new ValidationException(String.format(
                    "Compa range %.2f-%.2f overlaps with existing range %.2f-%.2f for performance bucket %d",
                    newMatrix.getCompaFrom(), newMatrix.getCompaTo(),
                    existing.getCompaFrom(), existing.getCompaTo(),
                    newMatrix.getPerfBucket()
                ));
            }
        }
    }

    /**
     * Validate no overlapping ranges for bulk update
     */
    private void validateNoOverlappingRangesBulk(String clientId, List<AdjustmentMatrix> matrices) {
        for (int i = 0; i < matrices.size(); i++) {
            for (int j = i + 1; j < matrices.size(); j++) {
                AdjustmentMatrix matrix1 = matrices.get(i);
                AdjustmentMatrix matrix2 = matrices.get(j);
                
                if (matrix1.getPerfBucket().equals(matrix2.getPerfBucket()) &&
                    rangesOverlap(matrix1.getCompaFrom(), matrix1.getCompaTo(),
                                matrix2.getCompaFrom(), matrix2.getCompaTo())) {
                    throw new ValidationException(String.format(
                        "Overlapping compa ranges found: %.2f-%.2f and %.2f-%.2f for performance bucket %d",
                        matrix1.getCompaFrom(), matrix1.getCompaTo(),
                        matrix2.getCompaFrom(), matrix2.getCompaTo(),
                        matrix1.getPerfBucket()
                    ));
                }
            }
        }
    }

    /**
     * Check if two ranges overlap
     */
    private boolean rangesOverlap(BigDecimal from1, BigDecimal to1, BigDecimal from2, BigDecimal to2) {
        return from1.compareTo(to2) < 0 && from2.compareTo(to1) < 0;
    }

    /**
     * Generate unique matrix ID
     */
    private String generateMatrixId(String clientId, AdjustmentMatrix matrix) {
        return String.format("%s_m_%d_%.2f_%.2f", 
            clientId, 
            matrix.getPerfBucket(), 
            matrix.getCompaFrom(), 
            matrix.getCompaTo()
        );
    }
}
