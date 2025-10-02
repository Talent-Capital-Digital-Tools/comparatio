package talentcapitalme.com.comparatio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Individual compensation calculation request")
public class CalcRequest {
    @NotNull
    @DecimalMin("0")
    @Schema(description = "Current employee salary", example = "75000.00")
    private BigDecimal currentSalary;
    
    @NotNull @DecimalMin("0.01")
    @Schema(description = "Mid-point of salary scale for the job grade", example = "80000.00")
    private BigDecimal midOfScale;
    
    @NotNull @Min(0)
    @Schema(description = "Years of experience", example = "5")
    private Integer yearsExperience;
    
    @NotNull @Min(1) @Max(5)
    @Schema(description = "Performance rating (1-5)", example = "4")
    private Integer performanceRating;
    
    @Schema(description = "Employee code/ID", example = "EMP001")
    private String employeeCode;
    
    @Schema(description = "Employee name", example = "John Doe")
    private String employeeName;
    
    @Schema(description = "Job title", example = "Software Engineer")
    private String jobTitle;
    
    @Schema(description = "Calculation date (optional, defaults to today)", example = "2024-01-15")
    private LocalDate asOf; // optional; defaults to today
}