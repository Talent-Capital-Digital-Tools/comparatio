package talentcapitalme.com.comparatio.dto;

import lombok.Data;
import talentcapitalme.com.comparatio.enumeration.UserRole;

@Data
public class UserResponse {
    private String id;
    private String email;
    private String username;
    private String fullName;
    private UserRole role;
    private String clientId;
    private String industry;
    private String avatarUrl;
}