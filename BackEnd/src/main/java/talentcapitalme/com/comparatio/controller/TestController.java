package talentcapitalme.com.comparatio.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import talentcapitalme.com.comparatio.entity.User;
import talentcapitalme.com.comparatio.enumeration.UserRole;
import talentcapitalme.com.comparatio.service.UserManagementService;

import java.util.List;

/**
 * Test Controller
 * 
 * Purpose: Development and testing utilities for multi-tenant functionality
 * - Test data creation and setup
 * - Multi-tenant isolation verification
 * - Development environment testing
 * - WARNING: This controller should be removed in production
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private UserManagementService userManagementService;

    /**
     * Test endpoint to create sample CLIENT_ADMIN users and verify separation
     */
    @PostMapping("/setup-clients")
    public ResponseEntity<String> setupTestClients() {
        log.info("Test Controller: Setting up test CLIENT_ADMIN users for development testing");
        try {
            // Create test CLIENT_ADMIN users
            User client1 = new User();
            client1.setUsername("hr_admin");
            client1.setEmail("hr@test.com");
            client1.setFullName("HR Department Admin");
            client1.setPasswordHash("password123"); // In real app, this would be properly hashed
            client1.setRole(UserRole.CLIENT_ADMIN);
            client1.setName("HR Department");
            client1.setActive(true);
            
            User client2 = new User();
            client2.setUsername("finance_admin");
            client2.setEmail("finance@test.com");
            client2.setFullName("Finance Department Admin");
            client2.setPasswordHash("password123"); // In real app, this would be properly hashed
            client2.setRole(UserRole.CLIENT_ADMIN);
            client2.setName("Finance Department");
            client2.setActive(true);
            
            log.info("Test Controller: Creating test client 1: HR Department");
            User savedClient1 = userManagementService.createClientAdmin(client1);
            log.info("Test Controller: Creating test client 2: Finance Department");
            User savedClient2 = userManagementService.createClientAdmin(client2);
            
            log.info("Test Controller: Successfully created {} test CLIENT_ADMIN users with default matrices", 2);
            return ResponseEntity.ok(
                "Successfully created test CLIENT_ADMIN users:\n" +
                "1. " + savedClient1.getName() + " (ID: " + savedClient1.getId() + ")\n" +
                "2. " + savedClient2.getName() + " (ID: " + savedClient2.getId() + ")\n" +
                "Each CLIENT_ADMIN now has its own set of default matrices."
            );
        } catch (Exception e) {
            log.error("Test Controller: Error creating test CLIENT_ADMIN users: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error creating CLIENT_ADMIN users: " + e.getMessage());
        }
    }
    
    /**
     * List all CLIENT_ADMIN users for testing
     */
    @GetMapping("/clients")
    public ResponseEntity<List<User>> listClients() {
        log.info("Test Controller: Listing all CLIENT_ADMIN users for testing purposes");
        List<User> clients = userManagementService.getAllClientAdmins();
        log.info("Test Controller: Found {} CLIENT_ADMIN users", clients.size());
        return ResponseEntity.ok(clients);
    }
}