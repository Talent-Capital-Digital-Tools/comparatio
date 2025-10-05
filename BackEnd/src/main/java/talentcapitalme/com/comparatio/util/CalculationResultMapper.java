package talentcapitalme.com.comparatio.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import talentcapitalme.com.comparatio.dto.BulkRowResult;
import talentcapitalme.com.comparatio.entity.CalculationResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping CalculationResult entities to BulkRowResult DTOs
 * Centralizes the conversion logic to avoid code duplication across controllers
 */
@Slf4j
@Component
public class CalculationResultMapper {

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
                .performanceRating5(result.getPerfBucket() == 3 ? 5 : result.getPerfBucket() == 2 ? 3 : 2)
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
