package talentcapitalme.com.comparatio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import talentcapitalme.com.comparatio.dto.MatrixValidationResult;
import talentcapitalme.com.comparatio.entity.AdjustmentMatrix;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatrixValidationService implements IMatrixValidationService {

    /**
     * Comprehensive validation of matrix configuration
     */
    public MatrixValidationResult validateMatrixConfiguration(List<AdjustmentMatrix> matrices) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Group matrices by performance bucket
        Map<Integer, List<AdjustmentMatrix>> groupedMatrices = matrices.stream()
                .collect(Collectors.groupingBy(AdjustmentMatrix::getPerfBucket));
        
        // Validate each performance bucket
        for (int bucket = 1; bucket <= 3; bucket++) {
            List<AdjustmentMatrix> bucketMatrices = groupedMatrices.getOrDefault(bucket, new ArrayList<>());
            validatePerformanceBucket(bucket, bucketMatrices, errors, warnings);
        }
        
        // Check for gaps in compa ratio coverage
        validateCompaRatioCoverage(groupedMatrices, errors, warnings);
        
        // Check for business logic issues
        validateBusinessLogic(matrices, warnings);
        
        boolean isValid = errors.isEmpty();
        String summary = generateValidationSummary(matrices.size(), errors.size(), warnings.size());
        
        return MatrixValidationResult.builder()
                .isValid(isValid)
                .errors(errors)
                .warnings(warnings)
                .summary(summary)
                .totalMatrices(matrices.size())
                .build();
    }

    /**
     * Validate a specific performance bucket
     */
    private void validatePerformanceBucket(int bucket, List<AdjustmentMatrix> matrices, 
                                         List<String> errors, List<String> warnings) {
        if (matrices.isEmpty()) {
            errors.add(String.format("Performance bucket %d has no matrices", bucket));
            return;
        }
        
        // Check for overlapping ranges
        for (int i = 0; i < matrices.size(); i++) {
            for (int j = i + 1; j < matrices.size(); j++) {
                AdjustmentMatrix matrix1 = matrices.get(i);
                AdjustmentMatrix matrix2 = matrices.get(j);
                
                if (rangesOverlap(matrix1.getCompaFrom(), matrix1.getCompaTo(),
                                matrix2.getCompaFrom(), matrix2.getCompaTo())) {
                    errors.add(String.format(
                        "Performance bucket %d: Overlapping ranges %.2f-%.2f and %.2f-%.2f",
                        bucket, matrix1.getCompaFrom(), matrix1.getCompaTo(),
                        matrix2.getCompaFrom(), matrix2.getCompaTo()
                    ));
                }
            }
        }
        
        // Check for gaps in coverage
        List<AdjustmentMatrix> sortedMatrices = matrices.stream()
                .sorted((m1, m2) -> m1.getCompaFrom().compareTo(m2.getCompaFrom()))
                .collect(Collectors.toList());
        
        for (int i = 0; i < sortedMatrices.size() - 1; i++) {
            AdjustmentMatrix current = sortedMatrices.get(i);
            AdjustmentMatrix next = sortedMatrices.get(i + 1);
            
            if (current.getCompaTo().compareTo(next.getCompaFrom()) < 0) {
                warnings.add(String.format(
                    "Performance bucket %d: Gap between %.2f-%.2f and %.2f-%.2f",
                    bucket, current.getCompaFrom(), current.getCompaTo(),
                    next.getCompaFrom(), next.getCompaTo()
                ));
            }
        }
        
        // Check for percentage consistency
        validatePercentageConsistency(bucket, matrices, warnings);
    }

    /**
     * Validate compa ratio coverage across all buckets
     */
    private void validateCompaRatioCoverage(Map<Integer, List<AdjustmentMatrix>> groupedMatrices,
                                          List<String> errors, List<String> warnings) {
        // Check if all buckets cover the standard compa ratio range (0.0 to 2.0)
        BigDecimal minCompa = BigDecimal.valueOf(0.0);
        BigDecimal maxCompa = BigDecimal.valueOf(2.0);
        
        for (int bucket = 1; bucket <= 3; bucket++) {
            List<AdjustmentMatrix> matrices = groupedMatrices.getOrDefault(bucket, new ArrayList<>());
            
            if (matrices.isEmpty()) {
                errors.add(String.format("Performance bucket %d has no matrices", bucket));
                continue;
            }
            
            BigDecimal bucketMin = matrices.stream()
                    .map(AdjustmentMatrix::getCompaFrom)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            
            BigDecimal bucketMax = matrices.stream()
                    .map(AdjustmentMatrix::getCompaTo)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            
            if (bucketMin.compareTo(minCompa) > 0) {
                warnings.add(String.format(
                    "Performance bucket %d: Coverage starts at %.2f, consider starting from 0.0",
                    bucket, bucketMin
                ));
            }
            
            if (bucketMax.compareTo(maxCompa) < 0) {
                warnings.add(String.format(
                    "Performance bucket %d: Coverage ends at %.2f, consider extending to 2.0+",
                    bucket, bucketMax
                ));
            }
        }
    }

    /**
     * Validate business logic consistency
     */
    private void validateBusinessLogic(List<AdjustmentMatrix> matrices, List<String> warnings) {
        // Check if higher performance buckets have higher percentages
        Map<Integer, List<AdjustmentMatrix>> grouped = matrices.stream()
                .collect(Collectors.groupingBy(AdjustmentMatrix::getPerfBucket));
        
        for (int bucket = 1; bucket <= 2; bucket++) {
            List<AdjustmentMatrix> currentBucket = grouped.getOrDefault(bucket, new ArrayList<>());
            List<AdjustmentMatrix> nextBucket = grouped.getOrDefault(bucket + 1, new ArrayList<>());
            
            if (!currentBucket.isEmpty() && !nextBucket.isEmpty()) {
                BigDecimal currentAvg = currentBucket.stream()
                        .map(AdjustmentMatrix::getPctLt5Years)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(currentBucket.size()), 2, java.math.RoundingMode.HALF_UP);
                
                BigDecimal nextAvg = nextBucket.stream()
                        .map(AdjustmentMatrix::getPctLt5Years)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(nextBucket.size()), 2, java.math.RoundingMode.HALF_UP);
                
                if (currentAvg.compareTo(nextAvg) > 0) {
                    warnings.add(String.format(
                        "Performance bucket %d has higher average percentage (%.2f%%) than bucket %d (%.2f%%)",
                        bucket, currentAvg, bucket + 1, nextAvg
                    ));
                }
            }
        }
        
        // Check for extreme percentage values
        for (AdjustmentMatrix matrix : matrices) {
            if (matrix.getPctLt5Years().compareTo(BigDecimal.valueOf(50)) > 0) {
                warnings.add(String.format(
                    "High percentage for <5 years experience: %.2f%% in bucket %d range %.2f-%.2f",
                    matrix.getPctLt5Years(), matrix.getPerfBucket(),
                    matrix.getCompaFrom(), matrix.getCompaTo()
                ));
            }
            
            if (matrix.getPctGte5Years().compareTo(BigDecimal.valueOf(50)) > 0) {
                warnings.add(String.format(
                    "High percentage for >=5 years experience: %.2f%% in bucket %d range %.2f-%.2f",
                    matrix.getPctGte5Years(), matrix.getPerfBucket(),
                    matrix.getCompaFrom(), matrix.getCompaTo()
                ));
            }
        }
    }

    /**
     * Validate percentage consistency within a bucket
     */
    private void validatePercentageConsistency(int bucket, List<AdjustmentMatrix> matrices,
                                             List<String> warnings) {
        // Check if percentages generally increase with compa ratio
        List<AdjustmentMatrix> sorted = matrices.stream()
                .sorted((m1, m2) -> m1.getCompaFrom().compareTo(m2.getCompaFrom()))
                .collect(Collectors.toList());
        
        for (int i = 0; i < sorted.size() - 1; i++) {
            AdjustmentMatrix current = sorted.get(i);
            AdjustmentMatrix next = sorted.get(i + 1);
            
            if (current.getPctLt5Years().compareTo(next.getPctLt5Years()) > 0) {
                warnings.add(String.format(
                    "Performance bucket %d: Percentage decreases from %.2f%% to %.2f%% as compa ratio increases",
                    bucket, current.getPctLt5Years(), next.getPctLt5Years()
                ));
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
     * Generate validation summary
     */
    private String generateValidationSummary(int totalMatrices, int errorCount, int warningCount) {
        if (errorCount == 0 && warningCount == 0) {
            return String.format("✓ Matrix configuration is valid (%d matrices)", totalMatrices);
        } else if (errorCount == 0) {
            return String.format("⚠ Matrix configuration has %d warnings (%d matrices)", warningCount, totalMatrices);
        } else {
            return String.format("✗ Matrix configuration has %d errors and %d warnings (%d matrices)", 
                    errorCount, warningCount, totalMatrices);
        }
    }
}
