package talentcapitalme.com.comparatio.service;

import talentcapitalme.com.comparatio.entity.User;

import java.util.List;

/**
 * Interface for User Management Service operations
 */
public interface IUserManagementService {
    
    /**
     * Get all CLIENT_ADMIN users (replacing client functionality)
     */
    List<User> getAllClientAdmins();
    
    /**
     * Get CLIENT_ADMIN user by ID
     */
    User getClientAdminById(String id);
    
    /**
     * Create a new CLIENT_ADMIN user and seed default matrices
     */
    User createClientAdmin(User user);
    
    /**
     * Update CLIENT_ADMIN user
     */
    User updateClientAdmin(String id, User userUpdate);
    
    /**
     * Delete CLIENT_ADMIN user
     */
    void deleteClientAdmin(String id);
    
    /**
     * Activate CLIENT_ADMIN user
     */
    User activateClientAdmin(String id);
    
    /**
     * Deactivate CLIENT_ADMIN user
     */
    User deactivateClientAdmin(String id);
}
