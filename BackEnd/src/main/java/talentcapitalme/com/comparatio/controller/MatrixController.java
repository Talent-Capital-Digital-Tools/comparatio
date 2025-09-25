package talentcapitalme.com.comparatio.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import talentcapitalme.com.comparatio.entity.AdjustmentMatrix;
import talentcapitalme.com.comparatio.exception.NotFoundException;
import talentcapitalme.com.comparatio.exception.ValidationException;
import talentcapitalme.com.comparatio.repository.AdjustmentMatrixRepository;
import talentcapitalme.com.comparatio.service.MatrixSeederService;

import java.util.List;

/**
 * Matrix Management Controller
 * 
 * Purpose: Handles adjustment matrix operations for compensation calculations
 * - Matrix retrieval and management for specific clients
 * - Matrix seeding and initialization
 * - Client-specific matrix access control
 * - Matrix validation and business rule enforcement
 */
@Slf4j
@RestController
@RequestMapping("/api/matrix")
@RequiredArgsConstructor
public class MatrixController {
    private final AdjustmentMatrixRepository repo;
    private final MatrixSeederService seeder;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<AdjustmentMatrix> list(@RequestParam(required = true) String clientId) {
        log.info("Matrix Management Controller: Retrieving matrices for client: {}", clientId);
        // SUPER_ADMIN can view matrices for any client - clientId is required
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new ValidationException("Client ID is required");
        }
        List<AdjustmentMatrix> matrices = repo.findByClientIdAndActiveTrue(clientId);
        log.info("Matrix Management Controller: Retrieved {} matrices for client: {}", matrices.size(), clientId);
        return matrices;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public AdjustmentMatrix create(@RequestParam(required = true) String clientId,
                                   @RequestBody AdjustmentMatrix m) {
        log.info("Matrix Management Controller: Creating new matrix for client: {}", clientId);
        // SUPER_ADMIN creates matrices for specific client
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new ValidationException("Client ID is required");
        }
        m.setClientId(clientId);
        AdjustmentMatrix saved = repo.save(m);
        log.info("Matrix Management Controller: Matrix created successfully with ID: {} for client: {}", 
                saved.getId(), clientId);
        return saved;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public AdjustmentMatrix update(@PathVariable String id,
                                   @RequestParam(required = true) String clientId,
                                   @RequestBody AdjustmentMatrix m) {
        log.info("Matrix Management Controller: Updating matrix ID: {} for client: {}", id, clientId);
        // SUPER_ADMIN updates matrices for specific client
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new ValidationException("Client ID is required");
        }
        
        // Verify the matrix exists and belongs to the specified client
        AdjustmentMatrix existing = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Adjustment matrix not found"));
        
        if (!clientId.equals(existing.getClientId())) {
            throw new ValidationException("Matrix does not belong to the specified client");
        }
        
        m.setId(id);
        m.setClientId(clientId);
        AdjustmentMatrix updated = repo.save(m);
        log.info("Matrix Management Controller: Matrix updated successfully for ID: {} and client: {}", id, clientId);
        return updated;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id,
                       @RequestParam(required = true) String clientId) {
        log.info("Matrix Management Controller: Deleting matrix ID: {} for client: {}", id, clientId);
        // SUPER_ADMIN deletes matrices for specific client
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new ValidationException("Client ID is required");
        }
        
        AdjustmentMatrix matrix = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Adjustment matrix not found"));
        
        if (!clientId.equals(matrix.getClientId())) {
            throw new ValidationException("Matrix does not belong to the specified client");
        }
        
        repo.deleteById(id);
        log.info("Matrix Management Controller: Matrix deleted successfully for ID: {} and client: {}", id, clientId);
    }

    @PostMapping("/seed-client")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> seedClientMatrices(@RequestParam(required = true) String clientId) {
        log.info("Matrix Management Controller: Seeding default matrices for client: {}", clientId);
        // SUPER_ADMIN can seed default matrices for a new client
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new ValidationException("Client ID is required");
        }
        
        if (repo.existsByClientId(clientId)) {
            throw new ValidationException("Matrices already exist for client: " + clientId);
        }
        
        seeder.seedDefaultsForClient(clientId);
        log.info("Matrix Management Controller: Default matrices seeded successfully for client: {}", clientId);
        return ResponseEntity.ok("Default matrices created for client: " + clientId);
    }
    
}
