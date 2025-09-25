package talentcapitalme.com.comparatio.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import talentcapitalme.com.comparatio.entity.AdjustmentMatrix;
import talentcapitalme.com.comparatio.exception.ValidationException;
import talentcapitalme.com.comparatio.repository.AdjustmentMatrixRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MatrixSeederService {

    private final AdjustmentMatrixRepository repo;

    /**
     * Seed default matrices for a client if none exist.
     * Idempotent: if any matrix exists for the client, it throws ValidationException (same behavior as controller).
     */
    public void seedDefaultsForClient(String clientId) {
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new ValidationException("Client ID is required");
        }
        if (repo.existsByClientId(clientId)) {
            throw new ValidationException("Matrices already exist for client: " + clientId);
        }

        // Performance bucket 3 (exceeds targets)
        seedRow(clientId, 3, 0.00, 0.70, 21, 25);
        seedRow(clientId, 3, 0.71, 0.85, 17, 21);
        seedRow(clientId, 3, 0.86, 1.00, 12, 17);
        seedRow(clientId, 3, 1.01, 1.15, 8, 12);
        seedRow(clientId, 3, 1.16, 1.30, 6, 8);
        seedRow(clientId, 3, 1.30, 9.99, 0, 0);

        // Performance bucket 2 (meets targets)
        seedRow(clientId, 2, 0.00, 0.70, 15, 17);
        seedRow(clientId, 2, 0.71, 0.85, 12, 17);
        seedRow(clientId, 2, 0.86, 1.00, 8, 12);
        seedRow(clientId, 2, 1.01, 1.15, 6, 8);
        seedRow(clientId, 2, 1.16, 1.30, 4, 6);
        seedRow(clientId, 2, 1.30, 9.99, 0, 0);

        // Performance bucket 1 (partially meets)
        seedRow(clientId, 1, 0.00, 0.70, 8, 12);
        seedRow(clientId, 1, 0.71, 0.85, 6, 8);
        seedRow(clientId, 1, 0.86, 1.00, 4, 6);
        seedRow(clientId, 1, 1.01, 1.15, 0, 4);
        seedRow(clientId, 1, 1.16, 9.99, 0, 0);
    }

    private void seedRow(String clientId, int perf, double from, double to, double l, double r) {
        repo.save(AdjustmentMatrix.builder()
                .id(clientId + "_m_" + perf + "_" + from + "_" + to)
                .clientId(clientId)
                .perfBucket(perf)
                .compaFrom(BigDecimal.valueOf(from))
                .compaTo(BigDecimal.valueOf(to))
                .pctLt5Years(BigDecimal.valueOf(l))
                .pctGte5Years(BigDecimal.valueOf(r))
                .effectiveFrom(LocalDate.of(2025, 1, 1))
                .effectiveTo(null)
                .active(true)
                .build());
    }
}


