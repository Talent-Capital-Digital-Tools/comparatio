package talentcapitalme.com.comparatio.service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import talentcapitalme.com.comparatio.dto.ProfileResponse;
import talentcapitalme.com.comparatio.dto.ProfileUpdateRequest;
import talentcapitalme.com.comparatio.entity.User;
import talentcapitalme.com.comparatio.enumeration.UserRole;
import talentcapitalme.com.comparatio.exception.NotFoundException;
import talentcapitalme.com.comparatio.repository.UserRepository;
import talentcapitalme.com.comparatio.security.Authz;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

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
        if (userUpdate.getIndustry() != null) {
            existingUser.setIndustry(userUpdate.getIndustry());
        }
        
        // Update client fields for CLIENT_ADMIN users
        if (userUpdate.getRole() == UserRole.CLIENT_ADMIN || existingUser.getRole() == UserRole.CLIENT_ADMIN) {
            if (userUpdate.getName() != null) {
                existingUser.setName(userUpdate.getName());
            }
            if (userUpdate.getActive() != null) {
                existingUser.setActive(userUpdate.getActive());
            }
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

    /**
     * Get current user's profile
     */
    public ProfileResponse getCurrentUserProfile() {
        String currentUserId = Authz.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        return ProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .companyName(user.getName())
                .industry(user.getIndustry())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .active(user.getActive())
                .build();
    }

    /**
     * Update current user's profile
     */
    public ProfileResponse updateCurrentUserProfile(ProfileUpdateRequest request) {
        String currentUserId = Authz.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Update profile fields
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getCompanyName() != null) {
            user.setName(request.getCompanyName());
        }
        if (request.getIndustry() != null) {
            user.setIndustry(request.getIndustry());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        User savedUser = userRepository.save(user);
        
        return ProfileResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .companyName(savedUser.getName())
                .industry(savedUser.getIndustry())
                .avatarUrl(savedUser.getAvatarUrl())
                .role(savedUser.getRole().name())
                .active(savedUser.getActive())
                .build();
    }

    /**
     * Get user profile by ID (admin only)
     */
    public ProfileResponse getUserProfile(String userId) {
        Authz.requireUserManagementPermission();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        
        return ProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .companyName(user.getName())
                .industry(user.getIndustry())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .active(user.getActive())
                .build();
    }

    /**
     * Update user profile by ID (admin only)
     */
    public ProfileResponse updateUserProfile(String userId, ProfileUpdateRequest request) {
        Authz.requireUserManagementPermission();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // Update profile fields
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getCompanyName() != null) {
            user.setName(request.getCompanyName());
        }
        if (request.getIndustry() != null) {
            user.setIndustry(request.getIndustry());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        User savedUser = userRepository.save(user);
        
        return ProfileResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .companyName(savedUser.getName())
                .industry(savedUser.getIndustry())
                .avatarUrl(savedUser.getAvatarUrl())
                .role(savedUser.getRole().name())
                .active(savedUser.getActive())
                .build();
    }
}