package talentcapitalme.com.comparatio.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import talentcapitalme.com.comparatio.enumeration.UserRole;
import talentcapitalme.com.comparatio.enumeration.PerformanceRatingScale;
import talentcapitalme.com.comparatio.enumeration.Currency;

/**
 * User entity representing a user in the system.
 * Each user has a unique identifier, username, password hash, and role.
 * For CLIENT_ADMIN users, the entity also contains client information (name, active status).
 */


@Document("users")
@Data
@EqualsAndHashCode(callSuper = false)
public class User extends Audit {
    @Id
    private String id;                  // can be same as username
    @Indexed(unique = true)
    private String username;
    @Indexed(unique = true, sparse = true)
    private String email;

    private String passwordHash;        // BCrypt
    private UserRole role;                // SUPER_ADMIN | CLIENT_ADMIN

    // Client fields (merged from Client entity)
    private String name;                // company name (for CLIENT_ADMIN users)
    private Boolean active;             // enable/disable tenant (for CLIENT_ADMIN users)

    // profile
    private String fullName;
    private String industry;
    private String avatarUrl;
    
    // Performance rating configuration
    private PerformanceRatingScale performanceRatingScale = PerformanceRatingScale.FIVE_POINT; // Default to 5-point scale
    
    // Currency configuration
    private Currency currency = Currency.USD; // Default to USD for international compatibility


}