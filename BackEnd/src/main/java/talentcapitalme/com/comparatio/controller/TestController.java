package talentcapitalme.com.comparatio.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import talentcapitalme.com.comparatio.entity.Client;
import talentcapitalme.com.comparatio.service.ClientService;

import java.util.List;

/**
 * Test controller to verify multi-tenant functionality
 * This should be removed in production
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private ClientService clientService;

    /**
     * Test endpoint to create sample clients and verify separation
     */
    @PostMapping("/setup-clients")
    public ResponseEntity<String> setupTestClients() {
        try {
            // Create test clients
            Client client1 = Client.builder()
                    .name("HR Department")
                    .active(true)
                    .build();
            
            Client client2 = Client.builder()
                    .name("Finance Department")
                    .active(true)
                    .build();
            
            Client savedClient1 = clientService.createClient(client1);
            Client savedClient2 = clientService.createClient(client2);
            
            return ResponseEntity.ok(
                "Successfully created test clients:\n" +
                "1. " + savedClient1.getName() + " (ID: " + savedClient1.getId() + ")\n" +
                "2. " + savedClient2.getName() + " (ID: " + savedClient2.getId() + ")\n" +
                "Each client now has its own set of default matrices."
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating clients: " + e.getMessage());
        }
    }
    
    /**
     * List all clients for testing
     */
    @GetMapping("/clients")
    public ResponseEntity<List<Client>> listClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }
}