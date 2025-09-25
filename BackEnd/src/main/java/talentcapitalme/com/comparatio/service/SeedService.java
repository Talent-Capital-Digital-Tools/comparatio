package talentcapitalme.com.comparatio.service;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import talentcapitalme.com.comparatio.entity.AdjustmentMatrix;
import talentcapitalme.com.comparatio.entity.User;
import talentcapitalme.com.comparatio.enumeration.UserRole;
import talentcapitalme.com.comparatio.repository.AdjustmentMatrixRepository;
import talentcapitalme.com.comparatio.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SeedService implements CommandLineRunner {

    private final AdjustmentMatrixRepository matrixRepo;
    private final UserRepository userRepo;

    @Override
    public void run(String... args) {
        // Only seed if no matrices exist at all
        if (matrixRepo.count() > 0) return;
        
        // Get all existing CLIENT_ADMIN users and create default matrices for each
        List<User> clientAdmins = userRepo.findByRoleAndActiveTrue(UserRole.CLIENT_ADMIN);
        
        if (clientAdmins.isEmpty()) {
            System.out.println("No CLIENT_ADMIN users found. Matrices will be created when CLIENT_ADMIN users are added.");
            return;
        }
        
        for (User clientAdmin : clientAdmins) {
            if (!matrixRepo.existsByClientId(clientAdmin.getId())) {
                System.out.println("Creating default matrices for CLIENT_ADMIN user: " + clientAdmin.getId());
                seedMatricesForClient(clientAdmin.getId());
            }
        }
    }

    /**
     * Create default matrix structure for a specific CLIENT_ADMIN user
     */
    public void seedMatricesForClient(String clientId) {
        seedRow(clientId, 3, 0.00, 0.70, 21, 25);
        seedRow(clientId, 3, 0.71, 0.85, 17, 21);
        seedRow(clientId, 3, 0.86, 1.00, 12, 17);
        seedRow(clientId, 3, 1.01, 1.15, 8, 12);
        seedRow(clientId, 3, 1.16, 1.30, 6, 8);
        seedRow(clientId, 3, 1.30, 9.99, 0, 0);

        seedRow(clientId, 2, 0.00, 0.70, 15, 17);
        seedRow(clientId, 2, 0.71, 0.85, 12, 17);
        seedRow(clientId, 2, 0.86, 1.00, 8, 12);
        seedRow(clientId, 2, 1.01, 1.15, 6, 8);
        seedRow(clientId, 2, 1.16, 1.30, 4, 6);
        seedRow(clientId, 2, 1.30, 9.99, 0, 0);

        seedRow(clientId, 1, 0.00, 0.70, 8, 12);
        seedRow(clientId, 1, 0.71, 0.85, 6, 8);
        seedRow(clientId, 1, 0.86, 1.00, 4, 6);
        seedRow(clientId, 1, 1.01, 1.15, 0, 4);
        seedRow(clientId, 1, 1.16, 9.99, 0, 0);
    }

    private void seedRow(String clientId, int perf, double from, double to, double l, double r) {
        matrixRepo.save(AdjustmentMatrix.builder()
                .id(clientId + "_m_" + perf + "_" + from + "_" + to)
                .clientId(clientId) // Client-specific matrices
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
