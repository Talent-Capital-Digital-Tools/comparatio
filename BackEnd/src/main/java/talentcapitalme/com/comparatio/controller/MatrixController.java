package talentcapitalme.com.comparatio.controller;

import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/matrix")
@RequiredArgsConstructor
public class MatrixController {
    private final AdjustmentMatrixRepository repo;
    private final MatrixSeederService seeder;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<AdjustmentMatrix> list(@RequestParam(required = true) String clientId) {
        // SUPER_ADMIN can view matrices for any client - clientId is required
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new ValidationException("Client ID is required");
        }
        return repo.findByClientIdAndActiveTrue(clientId);
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public AdjustmentMatrix create(@RequestParam(required = true) String clientId,
                                   @RequestBody AdjustmentMatrix m) {
        // SUPER_ADMIN creates matrices for specific client
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new ValidationException("Client ID is required");
        }
        m.setClientId(clientId);
        return repo.save(m);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public AdjustmentMatrix update(@PathVariable String id,
                                   @RequestParam(required = true) String clientId,
                                   @RequestBody AdjustmentMatrix m) {
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
        return repo.save(m);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id,
                       @RequestParam(required = true) String clientId) {
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
    }

    @PostMapping("/seed-client")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> seedClientMatrices(@RequestParam(required = true) String clientId) {
        // SUPER_ADMIN can seed default matrices for a new client
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new ValidationException("Client ID is required");
        }
        
        if (repo.existsByClientId(clientId)) {
            throw new ValidationException("Matrices already exist for client: " + clientId);
        }
        
        seeder.seedDefaultsForClient(clientId);
        return ResponseEntity.ok("Default matrices created for client: " + clientId);
    }
    
}
