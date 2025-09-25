package talentcapitalme.com.comparatio.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("User Management Controller: Retrieving all users");
        List<User> users = userService.getAllUsers();
        // Remove password hashes from response
        users.forEach(user -> user.setPasswordHash(null));
        log.info("User Management Controller: Retrieved {} users", users.size());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        log.info("User Management Controller: Retrieving user by ID: {}", id);
        User user = userService.getUserById(id);
        // Remove password hash from response
        user.setPasswordHash(null);
        log.info("User Management Controller: Retrieved user: {} for ID: {}", user.getUsername(), id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User userUpdate) {
        log.info("User Management Controller: Updating user with ID: {}", id);
        User updatedUser = userService.updateUser(id, userUpdate);
        // Remove password hash from response
        updatedUser.setPasswordHash(null);
        log.info("User Management Controller: User updated successfully for ID: {}", id);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        log.info("User Management Controller: Deleting user with ID: {}", id);
        userService.deleteUser(id);
        log.info("User Management Controller: User deleted successfully for ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable String id, 
                                             @Valid @RequestBody ChangePasswordRequest request) {
        log.info("User Management Controller: Changing password for user ID: {}", id);
        userService.changePassword(id, request.getNewPassword());
        log.info("User Management Controller: Password changed successfully for user ID: {}", id);
        return ResponseEntity.ok().build();
    }
}