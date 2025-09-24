package talentcapitalme.com.comparatio.service;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import talentcapitalme.com.comparatio.entity.AdjustmentMatrix;
import talentcapitalme.com.comparatio.repository.AdjustmentMatrixRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class SeedService implements CommandLineRunner {

    private final AdjustmentMatrixRepository repo;

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;
        seedRow(3,0.00,0.70,21,25);
        seedRow(3,0.71,0.85,17,21);
        seedRow(3,0.86,1.00,12,17);
        seedRow(3,1.01,1.15,8,12);
        seedRow(3,1.16,1.30,6,8);
        seedRow(3,1.30,9.99,0,0);

        seedRow(2,0.00,0.70,15,17);
        seedRow(2,0.71,0.85,12,17);
        seedRow(2,0.86,1.00,8,12);
        seedRow(2,1.01,1.15,6,8);
        seedRow(2,1.16,1.30,4,6);
        seedRow(2,1.30,9.99,0,0);

        seedRow(1,0.00,0.70,8,12);
        seedRow(1,0.71,0.85,6,8);
        seedRow(1,0.86,1.00,4,6);
        seedRow(1,1.01,1.15,0,4);
        seedRow(1,1.16,9.99,0,0);
    }

    private void seedRow(int perf, double from, double to, double l, double r) {
        repo.save(AdjustmentMatrix.builder()
                .id("m_"+perf+"_"+from+"_"+to)
                .perfBucket(perf)
                .compaFrom(BigDecimal.valueOf(from))
                .compaTo(BigDecimal.valueOf(to))
                .pctLt5Years(BigDecimal.valueOf(l))
                .pctGte5Years(BigDecimal.valueOf(r))
                .effectiveFrom(LocalDate.of(2025,1,1))
                .effectiveTo(null)
                .active(true)
                .build());
    }
}
