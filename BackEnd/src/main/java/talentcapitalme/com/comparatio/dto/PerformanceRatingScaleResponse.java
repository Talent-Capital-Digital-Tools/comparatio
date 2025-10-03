package talentcapitalme.com.comparatio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import talentcapitalme.com.comparatio.enumeration.PerformanceRatingScale;

/**
 * DTO for performance rating scale response
 * Contains the current performance rating scale configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceRatingScaleResponse {
    
    private PerformanceRatingScale performanceRatingScale;
    private int maxRating;
    private String displayName;
    private String description;
    
    /**
     * Create response from performance rating scale enum
     */
    public static PerformanceRatingScaleResponse from(PerformanceRatingScale scale) {
        return PerformanceRatingScaleResponse.builder()
                .performanceRatingScale(scale)
                .maxRating(scale.getMaxRating())
                .displayName(scale.getDisplayName())
                .description(getDescription(scale))
                .build();
    }
    
    private static String getDescription(PerformanceRatingScale scale) {
        switch (scale) {
            case THREE_POINT:
                return "3-Point Scale: 1=Low Performance, 2=Medium Performance, 3=High Performance";
            case FIVE_POINT:
                return "5-Point Scale: 1-2=Low Performance, 3=Medium Performance, 4-5=High Performance";
            default:
                return "";
        }
    }
}

