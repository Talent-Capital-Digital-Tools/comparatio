package talentcapitalme.com.comparatio.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import talentcapitalme.com.comparatio.dto.CalcRequest;
import talentcapitalme.com.comparatio.dto.CalcResponse;
import talentcapitalme.com.comparatio.entity.AdjustmentMatrix;
import talentcapitalme.com.comparatio.entity.CalculationResult;
import talentcapitalme.com.comparatio.exception.MatrixNotFoundException;
import talentcapitalme.com.comparatio.exception.ValidationException;
import talentcapitalme.com.comparatio.repository.AdjustmentMatrixRepository;
import talentcapitalme.com.comparatio.repository.CalculationResultRepository;
import talentcapitalme.com.comparatio.security.Authz;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompensationService {

    private final AdjustmentMatrixRepository matrixRepo;
    private final CalculationResultRepository resultRepo;

    public CalcResponse calculate(CalcRequest req) {
        log.info("Starting individual calculation for employee: {}", req.getEmployeeCode());
        long startTime = System.currentTimeMillis();
        
        try {
            // Comprehensive input validation
            validateCalculationRequest(req);
            
            // Get client ID for matrix lookup
            String clientId = Authz.getCurrentUserClientId();
            log.debug("Using client ID: {} for calculation", clientId);

            LocalDate asOf = req.getAsOf() != null ? req.getAsOf() : LocalDate.now();

            // Calculate compa ratio
            BigDecimal compa = req.getCurrentSalary()
                    .divide(req.getMidOfScale(), 6, RoundingMode.HALF_UP);

            // Determine performance bucket
            int perfBucket = (req.getPerformanceRating() >= 4) ? 3 :
                    (req.getPerformanceRating() >= 2) ? 2 : 1;

            // Find appropriate adjustment matrix
            AdjustmentMatrix cell = matrixRepo.findClientActiveCell(perfBucket, compa, asOf, clientId)
                    .orElseThrow(() -> new MatrixNotFoundException("No adjustment matrix found for client '" + clientId + 
                        "'. Please contact your administrator to set up compensation matrices."));

            // Calculate percentage increase
            BigDecimal pct = (req.getYearsExperience() < 5) ? cell.getPctLt5Years() : cell.getPctGte5Years();
            BigDecimal newSalary = req.getCurrentSalary()
                    .multiply(BigDecimal.ONE.add(pct.movePointLeft(2)))
                    .setScale(2, RoundingMode.HALF_UP);

            // Save calculation result for audit
            CalculationResult result = CalculationResult.builder()
                    .clientId(clientId)
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
                    .build();
            
            resultRepo.save(result);
            
            long processingTime = System.currentTimeMillis() - startTime;
            log.info("Calculation completed for employee: {} in {}ms", req.getEmployeeCode(), processingTime);
            
            return new CalcResponse(compa, compaLabel(cell), pct, newSalary);
            
        } catch (Exception e) {
            log.error("Error calculating compensation for employee: {}", req.getEmployeeCode(), e);
            throw e;
        }
    }

    /**
     * Comprehensive validation of calculation request
     */
    private void validateCalculationRequest(CalcRequest req) {
        if (req == null) {
            throw new ValidationException("Calculation request cannot be null");
        }
        
        if (req.getCurrentSalary() == null) {
            throw new ValidationException("Current salary is required");
        }
        if (req.getMidOfScale() == null) {
            throw new ValidationException("Mid of scale is required");
        }
        if (req.getPerformanceRating() == null) {
            throw new ValidationException("Performance rating is required");
        }
        if (req.getYearsExperience() == null) {
            throw new ValidationException("Years of experience is required");
        }
        
        if (req.getCurrentSalary().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Current salary must be positive");
        }
        if (req.getMidOfScale().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Mid of scale must be positive");
        }
        if (req.getPerformanceRating() < 1 || req.getPerformanceRating() > 5) {
            throw new ValidationException("Performance rating must be between 1 and 5");
        }
        if (req.getYearsExperience() < 0) {
            throw new ValidationException("Years of experience cannot be negative");
        }
        
        // Business logic validation
        if (req.getCurrentSalary().compareTo(req.getMidOfScale().multiply(BigDecimal.valueOf(3))) > 0) {
            log.warn("Current salary is more than 3x mid of scale for employee: {}", req.getEmployeeCode());
        }
    }

    private String compaLabel(AdjustmentMatrix c) {
        BigDecimal from = c.getCompaFrom().multiply(BigDecimal.valueOf(100));
        BigDecimal to = c.getCompaTo().multiply(BigDecimal.valueOf(100));
        boolean open = c.getCompaTo().compareTo(BigDecimal.valueOf(9.99)) >= 0; // treat >= 9.99 as +
        return open ? from.stripTrailingZeros().toPlainString() + "%+"
                : from.stripTrailingZeros().toPlainString() + "%–" + to.stripTrailingZeros().toPlainString() + "%";
    }

}
