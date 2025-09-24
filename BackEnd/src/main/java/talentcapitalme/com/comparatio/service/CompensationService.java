package talentcapitalme.com.comparatio.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import talentcapitalme.com.comparatio.dto.CalcRequest;
import talentcapitalme.com.comparatio.dto.CalcResponse;
import talentcapitalme.com.comparatio.entity.AdjustmentMatrix;
import talentcapitalme.com.comparatio.entity.CalculationResult;
import talentcapitalme.com.comparatio.exception.MatrixNotFoundException;
import talentcapitalme.com.comparatio.exception.ValidationException;
import talentcapitalme.com.comparatio.repository.AdjustmentMatrixRepository;
import talentcapitalme.com.comparatio.repository.CalculationResultRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CompensationService {

    private final AdjustmentMatrixRepository matrixRepo;
    private final CalculationResultRepository resultRepo;

    public CalcResponse calculate(CalcRequest req) {
        // Validate input parameters
        if (req.getCurrentSalary() == null || req.getMidOfScale() == null) {
            throw new ValidationException("Current salary and mid of scale are required");
        }
        if (req.getCurrentSalary().compareTo(BigDecimal.ZERO) <= 0 || req.getMidOfScale().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Current salary and mid of scale must be positive values");
        }
        if (req.getPerformanceRating() < 1 || req.getPerformanceRating() > 5) {
            throw new ValidationException("Performance rating must be between 1 and 5");
        }

        LocalDate asOf = req.getAsOf() != null ? req.getAsOf() : LocalDate.now();

        BigDecimal compa = req.getCurrentSalary()
                .divide(req.getMidOfScale(), 6, RoundingMode.HALF_UP);

        int perfBucket = (req.getPerformanceRating() >= 4) ? 3 :
                (req.getPerformanceRating() >= 2) ? 2 : 1;

        AdjustmentMatrix cell = matrixRepo.findActiveCell(perfBucket, compa, asOf)
                .orElseThrow(() -> new MatrixNotFoundException("No adjustment matrix cell found for the given performance bucket, compa ratio, and date"));


        BigDecimal pct = (req.getYearsExperience() < 5) ? cell.getPctLt5Years() : cell.getPctGte5Years();
        BigDecimal newSalary = req.getCurrentSalary()
                .multiply(BigDecimal.ONE.add(pct.movePointLeft(2)))
                .setScale(2, RoundingMode.HALF_UP);


        // persist audit row (optional for individual)
        resultRepo.save(CalculationResult.builder()
                .batchId("single-" + Instant.now())
                .employeeCode(req.getEmployeeCode())
                .jobTitle(req.getJobTitle())
                .yearsExperience(req.getYearsExperience())
                .perfBucket(perfBucket)
                .currentSalary(req.getCurrentSalary())
                .midOfScale(req.getMidOfScale())
                .compaRatio(compa)
                .compaLabel(compaLabel(cell))
                .increasePct(pct)
                .newSalary(newSalary)
                .build());


        return new CalcResponse(compa, compaLabel(cell), pct, newSalary);
    }


    private String compaLabel(AdjustmentMatrix c) {
        BigDecimal from = c.getCompaFrom().multiply(BigDecimal.valueOf(100));
        BigDecimal to = c.getCompaTo().multiply(BigDecimal.valueOf(100));
        boolean open = c.getCompaTo().compareTo(BigDecimal.valueOf(9.99)) >= 0; // treat >= 9.99 as +
        return open ? from.stripTrailingZeros().toPlainString() + "%+"
                : from.stripTrailingZeros().toPlainString() + "%–" + to.stripTrailingZeros().toPlainString() + "%";
    }

}
