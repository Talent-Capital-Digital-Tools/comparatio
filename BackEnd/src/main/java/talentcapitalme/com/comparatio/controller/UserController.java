package talentcapitalme.com.comparatio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import talentcapitalme.com.comparatio.dto.ChangePasswordRequest;
import talentcapitalme.com.comparatio.entity.User;
import talentcapitalme.com.comparatio.service.UserService;

import java.util.List;

/**
 * User Management Controller
 * 
 * Purpose: Handles user profile and account management operations
 * - User profile retrieval and updates
 * - Password change functionality
 * - User account management
 * - Personal information updates
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile and account management")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get All Users", description = "Retrieve all users (admin only)")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("User Management Controller: Retrieving all users");
        List<User> users = userService.getAllUsers();
        // Remove password hashes from response
        users.forEach(user -> user.setPasswordHash(null));
        log.info("User Management Controller: Retrieved {} users", users.size());
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get User by ID", description = "Retrieve a specific user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@Parameter(description = "User ID") @PathVariable String id) {
        log.info("User Management Controller: Retrieving user by ID: {}", id);
        User user = userService.getUserById(id);
        // Remove password hash from response
        user.setPasswordHash(null);
        log.info("User Management Controller: Retrieved user: {} for ID: {}", user.getUsername(), id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update User", description = "Update user information")
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@Parameter(description = "User ID") @PathVariable String id, @RequestBody User userUpdate) {
        log.info("User Management Controller: Updating user with ID: {}", id);
        User updatedUser = userService.updateUser(id, userUpdate);
        // Remove password hash from response
        updatedUser.setPasswordHash(null);
        log.info("User Management Controller: User updated successfully for ID: {}", id);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Delete User", description = "Delete a user account")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@Parameter(description = "User ID") @PathVariable String id) {
        log.info("User Management Controller: Deleting user with ID: {}", id);
        userService.deleteUser(id);
        log.info("User Management Controller: User deleted successfully for ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Change Password", description = "Change user password")
    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(@Parameter(description = "User ID") @PathVariable String id, 
                                             @Valid @RequestBody ChangePasswordRequest request) {
        log.info("User Management Controller: Changing password for user ID: {}", id);
        userService.changePassword(id, request.getNewPassword());
        log.info("User Management Controller: Password changed successfully for user ID: {}", id);
        return ResponseEntity.ok().build();
    }
}