package talentcapitalme.com.comparatio.service;

import talentcapitalme.com.comparatio.dto.LoginRequest;
import talentcapitalme.com.comparatio.dto.RegisterRequest;
import talentcapitalme.com.comparatio.dto.TokenResponse;
import talentcapitalme.com.comparatio.entity.User;

/**
 * Interface for Auth Service operations
 */
public interface IAuthService {
    
    /**
     * Authenticate user and generate JWT token
     */
    TokenResponse login(LoginRequest request);
    
    /**
     * Register user (handles both initial admin and regular user registration)
     */
    User registerUser(RegisterRequest request);
}
