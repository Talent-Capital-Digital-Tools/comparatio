package talentcapitalme.com.comparatio.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import talentcapitalme.com.comparatio.enumeration.PerformanceRatingScale;

/**
 * DTO for performance rating scale update request
 * Allows clients to switch between 3-point and 5-point rating scales
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceRatingScaleUpdateRequest {
    
    @NotNull(message = "Performance rating scale is required")
    private PerformanceRatingScale performanceRatingScale;
}

