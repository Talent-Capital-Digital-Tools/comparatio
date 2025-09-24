package talentcapitalme.com.comparatio.dto;

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
public class CalcRequest {
    @NotNull
    @DecimalMin("0")
    private BigDecimal currentSalary;
    @NotNull @DecimalMin("0.01")
    private BigDecimal midOfScale;
    @NotNull @Min(0)
    private Integer yearsExperience;
    @NotNull @Min(1) @Max(5)
    private Integer performanceRating;
    private String employeeCode;
    private String jobTitle;
    private LocalDate asOf; // optional; defaults to today
}