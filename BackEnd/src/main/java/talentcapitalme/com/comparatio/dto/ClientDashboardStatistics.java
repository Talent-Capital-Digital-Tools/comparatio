package talentcapitalme.com.comparatio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for client dashboard statistics based on calculation results
 * Provides comprehensive analytics for client's compensation calculations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDashboardStatistics {
    
    // Basic Metrics
    private int totalEmployees;                // Total number of calculation results (employees)
    private BigDecimal totalCurrentSalary;     // Sum of all current salaries
    private BigDecimal totalNewSalary;         // Sum of all new salaries
    private BigDecimal totalPercentageChange;  // ((totalNewSalary - totalCurrentSalary) / totalCurrentSalary) * 100
    
    
    // Percentage Increase Analysis (based on increasePct field)
    private PercentageIncreaseAnalysis percentageIncreaseAnalysis;
    
    // Amount Increase Analysis (based on newSalary - currentSalary)
    private AmountIncreaseAnalysis amountIncreaseAnalysis;
    
    // Metadata
    private String clientId;
    private String lastUpdated;
    
    
    /**
     * Nested class for Percentage Increase Analysis
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PercentageIncreaseAnalysis {
        private BigDecimal minimum;    // Minimum percentage increase
        private BigDecimal maximum;    // Maximum percentage increase
        private BigDecimal average;    // Average percentage increase
    }
    
    /**
     * Nested class for Amount Increase Analysis
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AmountIncreaseAnalysis {
        private BigDecimal minimum;    // Minimum salary increase amount
        private BigDecimal maximum;    // Maximum salary increase amount
        private BigDecimal average;    // Average salary increase amount
    }
}
