package talentcapitalme.com.comparatio.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import talentcapitalme.com.comparatio.dto.BulkRowResult;
import talentcapitalme.com.comparatio.entity.CalculationResult;
import talentcapitalme.com.comparatio.service.PerformanceRatingService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping CalculationResult entities to BulkRowResult DTOs
 * Centralizes the conversion logic to avoid code duplication across controllers
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CalculationResultMapper {
    
    private final PerformanceRatingService performanceRatingService;

    /**
     * Convert performance bucket back to the correct rating based on user's scale
     * @param perfBucket the stored performance bucket (1, 2, or 3)
     * @return the correct performance rating for display
     */
    private int convertBucketToRating(int perfBucket) {
        var userScale = performanceRatingService.getUserPerformanceRatingScale();
        
        switch (userScale) {
            case THREE_POINT:
                // 3-point scale: bucket = rating (direct mapping)
                return perfBucket;
            case FIVE_POINT:
                // 5-point scale: convert back using reverse mapping
                // Since we map: 1→1, 2→2, 3→2, 4→3, 5→3
                // Reverse: bucket 1 → rating 1, bucket 2 → rating 2, bucket 3 → rating 4
                switch (perfBucket) {
                    case 1: return 1;
                    case 2: return 2; // Both rating 2 and 3 map to bucket 2, so show 2
                    case 3: return 3; // Both rating 4 and 5 map to bucket 3, so show 3
                    default: return 1;
                }
            default:
                log.warn("Unknown performance rating scale: {}, defaulting to 5-point conversion", userScale);
                return perfBucket == 3 ? 4 : perfBucket;
        }
    }

    /**
     * Convert list of CalculationResult entities to BulkRowResult DTOs
     * Used across multiple controllers for consistent mapping
     */
    public List<BulkRowResult> convertToBulkRowResults(List<CalculationResult> results) {
        return results.stream()
                .map(this::convertToRowResult)
                .collect(Collectors.toList());
    }

    /**
     * Convert single CalculationResult entity to BulkRowResult DTO
     * Centralized conversion logic with proper row indexing
     */
    public BulkRowResult convertToRowResult(CalculationResult result) {
        return BulkRowResult.builder()
                .rowIndex(0) // Will be set by calling method based on context
                .employeeCode(result.getEmployeeCode())
                .employeeName(result.getEmployeeName() != null ? result.getEmployeeName() : "N/A")
                .jobTitle(result.getJobTitle())
                .yearsExperience(result.getYearsExperience())
                .performanceRating5(convertBucketToRating(result.getPerfBucket()))
                .currentSalary(result.getCurrentSalary())
                .midOfScale(result.getMidOfScale())
                .compaRatio(result.getCompaRatio())
                .compaLabel(result.getCompaLabel())
                .increasePct(result.getIncreasePct())
                .newSalary(result.getNewSalary())
                .increaseAmount(result.getNewSalary().subtract(result.getCurrentSalary()))
                .build();
    }

    /**
     * Convert with proper row indexing starting from specified index
     */
    public List<BulkRowResult> convertToBulkRowResultsWithIndexing(List<CalculationResult> results, int startIndex) {
        List<BulkRowResult> mappedResults = convertToBulkRowResults(results);
        
        // Set proper row indices
        for (int i = 0; i < mappedResults.size(); i++) {
            BulkRowResult current = mappedResults.get(i);
            BulkRowResult updated = BulkRowResult.builder()
                    .rowIndex(startIndex + i + 1) // Excel rows start from 1
                    .employeeCode(current.getEmployeeCode())
                    .employeeName(current.getEmployeeName())
                    .jobTitle(current.getJobTitle())
                    .yearsExperience(current.getYearsExperience())
                    .performanceRating5(current.getPerformanceRating5())
                    .currentSalary(current.getCurrentSalary())
                    .midOfScale(current.getMidOfScale())
                    .compaRatio(current.getCompaRatio())
                    .compaLabel(current.getCompaLabel())
                    .increasePct(current.getIncreasePct())
                    .newSalary(current.getNewSalary())
                    .increaseAmount(current.getIncreaseAmount())
                    .build();
            
            mappedResults.set(i, updated);
        }
        
        return mappedResults;
    }
}
