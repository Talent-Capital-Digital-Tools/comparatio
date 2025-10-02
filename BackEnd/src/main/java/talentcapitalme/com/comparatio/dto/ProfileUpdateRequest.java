package talentcapitalme.com.comparatio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import talentcapitalme.com.comparatio.enumeration.PerformanceRatingScale;
import talentcapitalme.com.comparatio.enumeration.Currency;

/**
 * DTO for profile update request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;
    
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;
    
    @NotBlank(message = "Company name is required")
    @Size(max = 100, message = "Company name must not exceed 100 characters")
    private String companyName;
    
    @NotBlank(message = "Industry is required")
    @Size(max = 50, message = "Industry must not exceed 50 characters")
    private String industry;
    
    private String avatarUrl;  // Optional - can be set via file upload
    
    private PerformanceRatingScale performanceRatingScale; // Performance rating scale configuration
    
    private Currency currency; // Currency configuration
}
