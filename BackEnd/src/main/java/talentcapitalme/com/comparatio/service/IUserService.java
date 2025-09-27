package talentcapitalme.com.comparatio.service;

import talentcapitalme.com.comparatio.dto.ProfileResponse;
import talentcapitalme.com.comparatio.dto.ProfileUpdateRequest;
import talentcapitalme.com.comparatio.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * Interface for User Service operations
 */
public interface IUserService {
    
    /**
     * Get all users (admin only)
     */
    List<User> getAllUsers();
    
    /**
     * Get user by ID
     */
    User getUserById(String id);
    
    /**
     * Update user (admin only)
     */
    User updateUser(String id, User userUpdate);
    
    /**
     * Delete user (admin only)
     */
    void deleteUser(String id);
    
    /**
     * Change user password (admin only)
     */
    void changePassword(String id, String newPassword);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Get current user's profile
     */
    ProfileResponse getCurrentUserProfile();
    
    /**
     * Update current user's profile
     */
    ProfileResponse updateCurrentUserProfile(ProfileUpdateRequest request);
    
    /**
     * Get user profile by ID (admin only)
     */
    ProfileResponse getUserProfile(String userId);
    
    /**
     * Update user profile by ID (admin only)
     */
    ProfileResponse updateUserProfile(String userId, ProfileUpdateRequest request);
}
