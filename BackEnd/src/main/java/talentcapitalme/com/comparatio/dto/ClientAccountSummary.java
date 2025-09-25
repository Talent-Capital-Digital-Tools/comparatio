package talentcapitalme.com.comparatio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import talentcapitalme.com.comparatio.entity.User;

import java.time.Instant;

/**
 * DTO for displaying client account summary in the super admin dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientAccountSummary {
    
    private String id;
    private String companyName;
    private String contactPerson;
    private String email;
    private String industry;
    private String ratingScale;
    private boolean active;
    private Instant createdAt;
    private Instant lastLoginAt;
    private int totalEmployees;
    private int totalCalculations;
    private String status; // "Active", "Inactive", "Suspended"
    
    /**
     * Create ClientAccountSummary from User entity
     */
    public static ClientAccountSummary fromUser(User user) {
        return ClientAccountSummary.builder()
                .id(user.getId())
                .companyName(user.getName())
                .contactPerson(user.getUsername()) // Using username as contact person
                .email(user.getEmail())
                .industry("Technology") // Default industry, can be enhanced later
                .ratingScale("5/5") // Default rating, can be calculated from performance
                .active(user.getActive() != null ? user.getActive() : false)
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getUpdatedAt()) // Using updatedAt as lastLoginAt
                .totalEmployees(0) // Will be calculated from employee count
                .totalCalculations(0) // Will be calculated from calculation results
                .status(user.getActive() != null && user.getActive() ? "Active" : "Inactive")
                .build();
    }
}
