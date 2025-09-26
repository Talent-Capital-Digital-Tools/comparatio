package talentcapitalme.com.comparatio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BulkRowResult {
    private int rowIndex;
    private String employeeCode;
    private String employeeName;
    private String jobTitle;
    private Integer yearsExperience;
    private Integer performanceRating5;
    private BigDecimal currentSalary;
    private BigDecimal midOfScale;
    private BigDecimal compaRatio;
    private String compaLabel;
    private BigDecimal increasePct;
    private BigDecimal newSalary;
    private BigDecimal increaseAmount; // New salary - Current salary
    private String error; // null if ok
}