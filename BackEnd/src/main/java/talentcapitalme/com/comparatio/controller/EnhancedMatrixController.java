package talentcapitalme.com.comparatio.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import talentcapitalme.com.comparatio.dto.MatrixResponse;
import talentcapitalme.com.comparatio.dto.MatrixUpdateRequest;
import talentcapitalme.com.comparatio.dto.MatrixValidationResult;
import talentcapitalme.com.comparatio.entity.AdjustmentMatrix;
import talentcapitalme.com.comparatio.entity.User;
import talentcapitalme.com.comparatio.repository.UserRepository;
import talentcapitalme.com.comparatio.service.MatrixManagementService;
import talentcapitalme.com.comparatio.service.MatrixValidationService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enhanced Matrix Management Controller
 * 
 * Purpose: Provides professional matrix management for super administrators
 * - Advanced matrix CRUD operations with validation
 * - Bulk matrix updates and management
 * - Matrix statistics and analytics
 * - Client-specific matrix configuration
 * - Professional API with comprehensive error handling
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/matrix")
@RequiredArgsConstructor
public class EnhancedMatrixController {

    private final MatrixManagementService matrixService;
    private final UserRepository userRepository;
    private final MatrixValidationService validationService;

    /**
     * Get all matrices for a specific client with detailed information
     */
    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<MatrixResponse>> getClientMatrices(@PathVariable String clientId) {
        log.info("Retrieving matrices for client: {}", clientId);
        
        List<AdjustmentMatrix> matrices = matrixService.getClientMatrices(clientId);
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        List<MatrixResponse> responses = matrices.stream()
                .map(matrix -> MatrixResponse.fromEntity(matrix, client.getName()))
                .collect(Collectors.toList());
        
        log.info("Retrieved {} matrices for client {}", responses.size(), clientId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get matrices grouped by performance bucket
     */
    @GetMapping("/client/{clientId}/grouped")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getClientMatricesGrouped(@PathVariable String clientId) {
        log.info("Retrieving grouped matrices for client: {}", clientId);
        
        List<AdjustmentMatrix> matrices = matrixService.getClientMatrices(clientId);
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        Map<Integer, List<MatrixResponse>> grouped = matrices.stream()
                .map(matrix -> MatrixResponse.fromEntity(matrix, client.getName()))
                .collect(Collectors.groupingBy(MatrixResponse::getPerfBucket));
        
        Map<String, Object> response = Map.of(
            "clientId", clientId,
            "clientName", client.getName(),
            "totalMatrices", matrices.size(),
            "performanceBucket1", grouped.getOrDefault(1, List.of()),
            "performanceBucket2", grouped.getOrDefault(2, List.of()),
            "performanceBucket3", grouped.getOrDefault(3, List.of())
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific matrix by ID
     */
    @GetMapping("/{matrixId}/client/{clientId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<MatrixResponse> getMatrix(@PathVariable String matrixId, 
                                                   @PathVariable String clientId) {
        log.info("Retrieving matrix {} for client {}", matrixId, clientId);
        
        AdjustmentMatrix matrix = matrixService.getMatrixById(matrixId, clientId);
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        MatrixResponse response = MatrixResponse.fromEntity(matrix, client.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new matrix
     */
    @PostMapping("/client/{clientId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<MatrixResponse> createMatrix(@PathVariable String clientId,
                                                      @Valid @RequestBody MatrixUpdateRequest request) {
        log.info("Creating matrix for client {} with performance bucket {}", clientId, request.getPerfBucket());
        
        AdjustmentMatrix matrix = AdjustmentMatrix.builder()
                .perfBucket(request.getPerfBucket())
                .compaFrom(request.getCompaFrom())
                .compaTo(request.getCompaTo())
                .pctLt5Years(request.getPctLt5Years())
                .pctGte5Years(request.getPctGte5Years())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .active(request.getActive())
                .build();
        
        AdjustmentMatrix saved = matrixService.createMatrix(clientId, matrix);
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        MatrixResponse response = MatrixResponse.fromEntity(saved, client.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing matrix
     */
    @PutMapping("/{matrixId}/client/{clientId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<MatrixResponse> updateMatrix(@PathVariable String matrixId,
                                                      @PathVariable String clientId,
                                                      @Valid @RequestBody MatrixUpdateRequest request) {
        log.info("Updating matrix {} for client {}", matrixId, clientId);
        
        AdjustmentMatrix matrix = AdjustmentMatrix.builder()
                .perfBucket(request.getPerfBucket())
                .compaFrom(request.getCompaFrom())
                .compaTo(request.getCompaTo())
                .pctLt5Years(request.getPctLt5Years())
                .pctGte5Years(request.getPctGte5Years())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .active(request.getActive())
                .build();
        
        AdjustmentMatrix updated = matrixService.updateMatrix(matrixId, clientId, matrix);
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        MatrixResponse response = MatrixResponse.fromEntity(updated, client.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a matrix
     */
    @DeleteMapping("/{matrixId}/client/{clientId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteMatrix(@PathVariable String matrixId,
                                            @PathVariable String clientId) {
        log.info("Deleting matrix {} for client {}", matrixId, clientId);
        
        matrixService.deleteMatrix(matrixId, clientId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Bulk update matrices for a client
     */
    @PutMapping("/client/{clientId}/bulk")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<MatrixResponse>> bulkUpdateMatrices(@PathVariable String clientId,
                                                                  @Valid @RequestBody List<MatrixUpdateRequest> requests) {
        log.info("Bulk updating {} matrices for client {}", requests.size(), clientId);
        
        List<AdjustmentMatrix> matrices = requests.stream()
                .map(req -> AdjustmentMatrix.builder()
                        .perfBucket(req.getPerfBucket())
                        .compaFrom(req.getCompaFrom())
                        .compaTo(req.getCompaTo())
                        .pctLt5Years(req.getPctLt5Years())
                        .pctGte5Years(req.getPctGte5Years())
                        .effectiveFrom(req.getEffectiveFrom())
                        .effectiveTo(req.getEffectiveTo())
                        .active(req.getActive())
                        .build())
                .collect(Collectors.toList());
        
        List<AdjustmentMatrix> updated = matrixService.bulkUpdateMatrices(clientId, matrices);
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        List<MatrixResponse> responses = updated.stream()
                .map(matrix -> MatrixResponse.fromEntity(matrix, client.getName()))
                .collect(Collectors.toList());
        
        log.info("Bulk updated {} matrices for client {}", responses.size(), clientId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Reset matrices to default for a client
     */
    @PostMapping("/client/{clientId}/reset")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<MatrixResponse>> resetToDefaultMatrices(@PathVariable String clientId) {
        log.info("Resetting matrices to default for client {}", clientId);
        
        List<AdjustmentMatrix> defaultMatrices = matrixService.resetToDefaultMatrices(clientId);
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        List<MatrixResponse> responses = defaultMatrices.stream()
                .map(matrix -> MatrixResponse.fromEntity(matrix, client.getName()))
                .collect(Collectors.toList());
        
        log.info("Reset {} default matrices for client {}", responses.size(), clientId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get matrix statistics for a client
     */
    @GetMapping("/client/{clientId}/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getMatrixStats(@PathVariable String clientId) {
        log.info("Retrieving matrix statistics for client {}", clientId);
        
        List<AdjustmentMatrix> matrices = matrixService.getClientMatrices(clientId);
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        Map<Integer, Long> bucketCounts = matrices.stream()
                .collect(Collectors.groupingBy(AdjustmentMatrix::getPerfBucket, Collectors.counting()));
        
        java.time.Instant lastUpdated = matrices.stream()
                .map(AdjustmentMatrix::getUpdatedAt)
                .max(java.time.Instant::compareTo)
                .orElse(null);
        
        Map<String, Object> stats = Map.of(
            "clientId", clientId,
            "clientName", client.getName(),
            "totalMatrices", matrices.size(),
            "performanceBucket1Count", bucketCounts.getOrDefault(1, 0L),
            "performanceBucket2Count", bucketCounts.getOrDefault(2, 0L),
            "performanceBucket3Count", bucketCounts.getOrDefault(3, 0L),
            "hasActiveMatrices", !matrices.isEmpty(),
            "lastUpdated", lastUpdated
        );
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Validate matrix configuration for a client
     */
    @GetMapping("/client/{clientId}/validate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<MatrixValidationResult> validateMatrices(@PathVariable String clientId) {
        log.info("Validating matrix configuration for client {}", clientId);
        
        List<AdjustmentMatrix> matrices = matrixService.getClientMatrices(clientId);
        MatrixValidationResult result = validationService.validateMatrixConfiguration(matrices);
        
        log.info("Matrix validation completed for client {}: {} errors, {} warnings", 
                clientId, result.getErrorCount(), result.getWarningCount());
        
        return ResponseEntity.ok(result);
    }

    /**
     * Validate matrix configuration before saving
     */
    @PostMapping("/client/{clientId}/validate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<MatrixValidationResult> validateMatrixConfiguration(
            @PathVariable String clientId,
            @Valid @RequestBody List<MatrixUpdateRequest> requests) {
        log.info("Validating {} matrix configurations for client {}", requests.size(), clientId);
        
        List<AdjustmentMatrix> matrices = requests.stream()
                .map(req -> AdjustmentMatrix.builder()
                        .perfBucket(req.getPerfBucket())
                        .compaFrom(req.getCompaFrom())
                        .compaTo(req.getCompaTo())
                        .pctLt5Years(req.getPctLt5Years())
                        .pctGte5Years(req.getPctGte5Years())
                        .effectiveFrom(req.getEffectiveFrom())
                        .effectiveTo(req.getEffectiveTo())
                        .active(req.getActive())
                        .build())
                .collect(Collectors.toList());
        
        MatrixValidationResult result = validationService.validateMatrixConfiguration(matrices);
        
        log.info("Matrix validation completed: {} errors, {} warnings", 
                result.getErrorCount(), result.getWarningCount());
        
        return ResponseEntity.ok(result);
    }
}
