package talentcapitalme.com.comparatio.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import talentcapitalme.com.comparatio.enumeration.UserRole;

/**
 * User entity representing a user in the system.
 * Each user has a unique identifier, username, password hash, role, and associated client ID.
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
    private UserRole role;                // SUPER_ADMIN |
    private String clientId;            // null for SUPER_ADMIN (global)

    // profile
    private String fullName;
    private String Industry;
    private String avatarUrl;


}