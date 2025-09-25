package talentcapitalme.com.comparatio.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import talentcapitalme.com.comparatio.entity.User;
import talentcapitalme.com.comparatio.enumeration.UserRole;
import talentcapitalme.com.comparatio.exception.NotFoundException;
import talentcapitalme.com.comparatio.repository.UserRepository;
import talentcapitalme.com.comparatio.security.Authz;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all users (admin only)
     */
    public List<User> getAllUsers() {
        Authz.requireUserManagementPermission();
        return userRepository.findAll();
    }

    /**
     * Get user by ID
     */
    public User getUserById(String id) {
        Authz.requireUserManagementPermission();
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    /**
     * Update user (admin only)
     */
    public User updateUser(String id, User userUpdate) {
        Authz.requireUserManagementPermission();

        User existingUser = getUserById(id);

        // Update allowed fields
        if (userUpdate.getEmail() != null) {
            existingUser.setEmail(userUpdate.getEmail());
        }
        if (userUpdate.getUsername() != null) {
            existingUser.setUsername(userUpdate.getUsername());
        }
        if (userUpdate.getFullName() != null) {
            existingUser.setFullName(userUpdate.getFullName());
        }
        if (userUpdate.getRole() != null) {
            existingUser.setRole(userUpdate.getRole());
        }
        if (userUpdate.getClientId() != null) {
            existingUser.setClientId(userUpdate.getClientId());
        }
        if (userUpdate.getIndustry() != null) {
            existingUser.setIndustry(userUpdate.getIndustry());
        }

        return userRepository.save(existingUser);
    }

    /**
     * Delete user (admin only)
     */
    public void deleteUser(String id) {
        Authz.requireUserManagementPermission();
        
        User user = getUserById(id);
        // Prevent deletion of super admin users by non-super admins
        if (user.getRole() == UserRole.SUPER_ADMIN && Authz.getCurrentUserRole() != UserRole.SUPER_ADMIN) {
            throw new NotFoundException("Cannot delete super admin user");
        }
        
        userRepository.deleteById(id);
    }

    /**
     * Change user password (admin only)
     */
    public void changePassword(String id, String newPassword) {
        Authz.requireUserManagementPermission();
        
        User user = getUserById(id);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}