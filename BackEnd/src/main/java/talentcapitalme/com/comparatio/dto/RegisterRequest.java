package talentcapitalme.com.comparatio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import talentcapitalme.com.comparatio.enumeration.UserRole;
import talentcapitalme.com.comparatio.enumeration.PerformanceRatingScale;
import talentcapitalme.com.comparatio.enumeration.Currency;

@Data
public class RegisterRequest {


    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "Role is required")
    private UserRole role;

    private String name; 

    private String industry;
    
    private Boolean active; 
    
    private String avatarUrl;
    
    private PerformanceRatingScale performanceRatingScale; // Performance rating scale configuration
    
    private Currency currency; // Currency configuration 


}