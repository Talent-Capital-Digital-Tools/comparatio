package talentcapitalme.com.comparatio.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import talentcapitalme.com.comparatio.entity.User;
import talentcapitalme.com.comparatio.enumeration.UserRole;
import talentcapitalme.com.comparatio.exception.NotFoundException;
import talentcapitalme.com.comparatio.exception.ValidationException;
import talentcapitalme.com.comparatio.repository.AdjustmentMatrixRepository;
import talentcapitalme.com.comparatio.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserManagementService implements IUserManagementService {

    private final UserRepository userRepository;
    private final AdjustmentMatrixRepository matrixRepository;
    private final SeedService seedService;

    /**
     * Get all CLIENT_ADMIN users (replacing client functionality)
     */
    public List<User> getAllClientAdmins() {
        return userRepository.findByRoleAndActiveTrue(UserRole.CLIENT_ADMIN);
    }

    /**
     * Get CLIENT_ADMIN user by ID
     */
    public User getClientAdminById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        
        if (user.getRole() != UserRole.CLIENT_ADMIN) {
            throw new NotFoundException("User with id " + id + " is not a CLIENT_ADMIN");
        }
        
        return user;
    }

    /**
     * Create a new CLIENT_ADMIN user and seed default matrices
     */
    public User createClientAdmin(User user) {
        // Validate unique name
        if (userRepository.existsByName(user.getName())) {
            throw new ValidationException("CLIENT_ADMIN with name '" + user.getName() + "' already exists");
        }

        // Ensure it's a CLIENT_ADMIN
        user.setRole(UserRole.CLIENT_ADMIN);
        user.setActive(true); // New client admins are active by default
        
        User savedUser = userRepository.save(user);

        // Create default matrices for the new CLIENT_ADMIN
        seedService.seedMatricesForClient(savedUser.getId());

        return savedUser;
    }

    /**
     * Update existing CLIENT_ADMIN user
     */
    public User updateClientAdmin(String id, User userUpdate) {
        User existingUser = getClientAdminById(id);

        // Update fields
        if (userUpdate.getName() != null) {
            // Check name uniqueness if changing
            if (!userUpdate.getName().equals(existingUser.getName()) && 
                userRepository.existsByName(userUpdate.getName())) {
                throw new ValidationException("CLIENT_ADMIN with name '" + userUpdate.getName() + "' already exists");
            }
            existingUser.setName(userUpdate.getName());
        }
        
        if (userUpdate.getActive() != null) {
            existingUser.setActive(userUpdate.getActive());
        }

        return userRepository.save(existingUser);
    }

    /**
     * Delete CLIENT_ADMIN user and all associated data
     */
    public void deleteClientAdmin(String id) {
        // Verify the user exists and is a CLIENT_ADMIN
        getClientAdminById(id);
        
        // Delete all matrices for this CLIENT_ADMIN
        matrixRepository.deleteByClientId(id);
        
        // Delete the user
        userRepository.deleteById(id);
    }

    /**
     * Activate CLIENT_ADMIN user
     */
    public User activateClientAdmin(String id) {
        User user = getClientAdminById(id);
        user.setActive(true);
        return userRepository.save(user);
    }

    /**
     * Deactivate CLIENT_ADMIN user
     */
    public User deactivateClientAdmin(String id) {
        User user = getClientAdminById(id);
        user.setActive(false);
        return userRepository.save(user);
    }

    /**
     * Get active CLIENT_ADMIN users only
     */
    public List<User> getActiveClientAdmins() {
        return userRepository.findByRoleAndActiveTrue(UserRole.CLIENT_ADMIN);
    }
}
