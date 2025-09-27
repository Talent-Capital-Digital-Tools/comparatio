package talentcapitalme.com.comparatio.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import talentcapitalme.com.comparatio.entity.User;
import talentcapitalme.com.comparatio.service.IUserManagementService;

import java.util.List;

/**
 * Client Management Controller
 * 
 * Purpose: Handles client admin user management operations
 * - CRUD operations for client admin users
 * - Client activation and deactivation
 * - Client account management and monitoring
 * - Super admin only access for client oversight
 */
@Slf4j
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final IUserManagementService userManagementService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<User>> getAllClientAdmins() {
        log.info("Client Management Controller: Retrieving all client admin users");
        List<User> clientAdmins = userManagementService.getAllClientAdmins();
        log.info("Client Management Controller: Retrieved {} client admin users", clientAdmins.size());
        return ResponseEntity.ok(clientAdmins);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<User> getClientAdminById(@PathVariable String id) {
        log.info("Client Management Controller: Retrieving client admin by ID: {}", id);
        User clientAdmin = userManagementService.getClientAdminById(id);
        log.info("Client Management Controller: Retrieved client admin: {} for ID: {}", clientAdmin.getUsername(), id);
        return ResponseEntity.ok(clientAdmin);
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<User> createClientAdmin(@Valid @RequestBody User user) {
        log.info("Client Management Controller: Creating new client admin: {} for company: {}", user.getUsername(), user.getName());
        User createdClientAdmin = userManagementService.createClientAdmin(user);
        log.info("Client Management Controller: Client admin created successfully with ID: {} for company: {}", 
                createdClientAdmin.getId(), createdClientAdmin.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClientAdmin);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<User> updateClientAdmin(@PathVariable String id, @Valid @RequestBody User user) {
        log.info("Client Management Controller: Updating client admin with ID: {} for company: {}", id, user.getName());
        User updatedClientAdmin = userManagementService.updateClientAdmin(id, user);
        log.info("Client Management Controller: Client admin updated successfully for ID: {}", id);
        return ResponseEntity.ok(updatedClientAdmin);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteClientAdmin(@PathVariable String id) {
        log.info("Client Management Controller: Deleting client admin with ID: {}", id);
        userManagementService.deleteClientAdmin(id);
        log.info("Client Management Controller: Client admin deleted successfully for ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<User> activateClientAdmin(@PathVariable String id) {
        log.info("Client Management Controller: Activating client admin with ID: {}", id);
        User clientAdmin = userManagementService.activateClientAdmin(id);
        log.info("Client Management Controller: Client admin activated successfully for ID: {} - Company: {}", 
                id, clientAdmin.getName());
        return ResponseEntity.ok(clientAdmin);
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<User> deactivateClientAdmin(@PathVariable String id) {
        log.info("Client Management Controller: Deactivating client admin with ID: {}", id);
        User clientAdmin = userManagementService.deactivateClientAdmin(id);
        log.info("Client Management Controller: Client admin deactivated successfully for ID: {} - Company: {}", 
                id, clientAdmin.getName());
        return ResponseEntity.ok(clientAdmin);
    }
}