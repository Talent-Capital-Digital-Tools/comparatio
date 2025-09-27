package talentcapitalme.com.comparatio.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import talentcapitalme.com.comparatio.dto.ClientAccountSummary;
import talentcapitalme.com.comparatio.dto.ClientAccountsResponse;
import talentcapitalme.com.comparatio.dto.DashboardResponse;
import talentcapitalme.com.comparatio.service.DashboardService;
import java.util.List;

/**
 * Super Admin Dashboard Controller
 * 
 * Purpose: Provides comprehensive dashboard functionality for super administrators
 * - Client account overview and management
 * - System statistics and performance metrics
 * - Client status management (activate/deactivate)
 * - Paginated data access with sorting and filtering
 * - Real-time monitoring of all client activities
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Analytics and dashboard data (Super Admin only)")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get Dashboard Data", description = "Get paginated dashboard data with sorting")
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<DashboardResponse> getDashboard(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "companyName") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("Dashboard request - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                page, size, sortBy, sortDir);
        
        try {
            DashboardResponse response = dashboardService.getDashboard(page, size, sortBy, sortDir);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching dashboard data", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get Client Accounts", description = "Get client accounts with pagination and sorting")
    @GetMapping("/clients")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ClientAccountsResponse> getClientAccounts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("Client accounts request - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                page, size, sortBy, sortDir);
        
        try {
            ClientAccountsResponse response = dashboardService.getClientAccountsPaginated(page, size, sortBy, sortDir);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching client accounts", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get specific client account by ID
     * GET /api/admin/dashboard/clients/{clientId}
     */
    @GetMapping("/clients/{clientId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ClientAccountSummary> getClientAccount(@PathVariable String clientId) {
        log.info("Fetching client account by ID: {}", clientId);
        
        try {
            ClientAccountSummary client = dashboardService.getClientAccountById(clientId);
            return ResponseEntity.ok(client);
        } catch (RuntimeException e) {
            log.error("Client not found: {}", clientId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching client account", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Toggle client account status (active/inactive)
     * PUT /api/admin/dashboard/clients/{clientId}/toggle-status
     */
    @PutMapping("/clients/{clientId}/toggle-status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ClientAccountSummary> toggleClientStatus(@PathVariable String clientId) {
        log.info("Toggling client status for ID: {}", clientId);
        
        try {
            ClientAccountSummary client = dashboardService.toggleClientStatus(clientId);
            return ResponseEntity.ok(client);
        } catch (RuntimeException e) {
            log.error("Client not found or invalid: {}", clientId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error toggling client status", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Activate client account
     * PUT /api/admin/dashboard/clients/{clientId}/activate
     */
    @PutMapping("/clients/{clientId}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ClientAccountSummary> activateClient(@PathVariable String clientId) {
        log.info("Activating client account: {}", clientId);
        
        try {
            ClientAccountSummary client = dashboardService.getClientAccountById(clientId);
            if (client.isActive()) {
                return ResponseEntity.ok(client); // Already active
            }
            
            ClientAccountSummary updatedClient = dashboardService.toggleClientStatus(clientId);
            return ResponseEntity.ok(updatedClient);
        } catch (RuntimeException e) {
            log.error("Client not found: {}", clientId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error activating client", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deactivate client account
     * PUT /api/admin/dashboard/clients/{clientId}/deactivate
     */
    @PutMapping("/clients/{clientId}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ClientAccountSummary> deactivateClient(@PathVariable String clientId) {
        log.info("Deactivating client account: {}", clientId);
        
        try {
            ClientAccountSummary client = dashboardService.getClientAccountById(clientId);
            if (!client.isActive()) {
                return ResponseEntity.ok(client); // Already inactive
            }
            
            ClientAccountSummary updatedClient = dashboardService.toggleClientStatus(clientId);
            return ResponseEntity.ok(updatedClient);
        } catch (RuntimeException e) {
            log.error("Client not found: {}", clientId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deactivating client", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get dashboard statistics only
     * GET /api/admin/dashboard/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<DashboardResponse> getDashboardStats() {
        log.info("Fetching dashboard statistics");
        
        try {
            DashboardResponse response = dashboardService.getDashboard(0, 1, "companyName", "asc");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching dashboard statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
