package talentcapitalme.com.comparatio.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatrixUpdateRequest {
    
    @NotNull(message = "Performance bucket is required")
    @Min(value = 1, message = "Performance bucket must be between 1 and 3")
    @Max(value = 3, message = "Performance bucket must be between 1 and 3")
    private Integer perfBucket;
    
    @NotNull(message = "Compa 'from' value is required")
    @DecimalMin(value = "0.0", message = "Compa 'from' cannot be negative")
    private BigDecimal compaFrom;
    
    @NotNull(message = "Compa 'to' value is required")
    @DecimalMin(value = "0.01", message = "Compa 'to' must be positive")
    private BigDecimal compaTo;
    
    @NotNull(message = "Percentage for <5 years is required")
    @DecimalMin(value = "0.0", message = "Percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Percentage cannot exceed 100%")
    private BigDecimal pctLt5Years;
    
    @NotNull(message = "Percentage for >=5 years is required")
    @DecimalMin(value = "0.0", message = "Percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Percentage cannot exceed 100%")
    private BigDecimal pctGte5Years;
    
    
    @Builder.Default
    private Boolean active = true;
}
