package talentcapitalme.com.comparatio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import talentcapitalme.com.comparatio.enumeration.PerformanceRatingScale;
import talentcapitalme.com.comparatio.enumeration.Currency;

/**
 * DTO for user profile response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String companyName;  // name field from User entity
    private String industry;
    private String avatarUrl;    // profile image URL
    private String role;
    private Boolean active;      // for CLIENT_ADMIN users
    private PerformanceRatingScale performanceRatingScale; // performance rating scale configuration
    private Currency currency;   // currency configuration
}
