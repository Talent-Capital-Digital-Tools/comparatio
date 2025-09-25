package talentcapitalme.com.comparatio.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import talentcapitalme.com.comparatio.dto.ChangePasswordRequest;
import talentcapitalme.com.comparatio.entity.User;
import talentcapitalme.com.comparatio.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        // Remove password hashes from response
        users.forEach(user -> user.setPasswordHash(null));
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        // Remove password hash from response
        user.setPasswordHash(null);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User userUpdate) {
        User updatedUser = userService.updateUser(id, userUpdate);
        // Remove password hash from response
        updatedUser.setPasswordHash(null);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable String id, 
                                             @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}