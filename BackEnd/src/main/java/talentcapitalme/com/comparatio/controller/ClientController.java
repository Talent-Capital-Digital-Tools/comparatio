package talentcapitalme.com.comparatio.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import talentcapitalme.com.comparatio.entity.User;
import talentcapitalme.com.comparatio.service.UserManagementService;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final UserManagementService userManagementService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<User>> getAllClientAdmins() {
        List<User> clientAdmins = userManagementService.getAllClientAdmins();
        return ResponseEntity.ok(clientAdmins);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<User> getClientAdminById(@PathVariable String id) {
        User clientAdmin = userManagementService.getClientAdminById(id);
        return ResponseEntity.ok(clientAdmin);
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<User> createClientAdmin(@Valid @RequestBody User user) {
        User createdClientAdmin = userManagementService.createClientAdmin(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClientAdmin);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<User> updateClientAdmin(@PathVariable String id, @Valid @RequestBody User user) {
        User updatedClientAdmin = userManagementService.updateClientAdmin(id, user);
        return ResponseEntity.ok(updatedClientAdmin);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteClientAdmin(@PathVariable String id) {
        userManagementService.deleteClientAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<User> activateClientAdmin(@PathVariable String id) {
        User clientAdmin = userManagementService.activateClientAdmin(id);
        return ResponseEntity.ok(clientAdmin);
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<User> deactivateClientAdmin(@PathVariable String id) {
        User clientAdmin = userManagementService.deactivateClientAdmin(id);
        return ResponseEntity.ok(clientAdmin);
    }
}