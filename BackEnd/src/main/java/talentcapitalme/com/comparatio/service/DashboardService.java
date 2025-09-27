package talentcapitalme.com.comparatio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import talentcapitalme.com.comparatio.dto.ClientAccountSummary;
import talentcapitalme.com.comparatio.dto.ClientAccountsResponse;
import talentcapitalme.com.comparatio.dto.DashboardResponse;
import talentcapitalme.com.comparatio.dto.DashboardStats;
import talentcapitalme.com.comparatio.entity.User;
import talentcapitalme.com.comparatio.enumeration.UserRole;
import talentcapitalme.com.comparatio.repository.AdjustmentMatrixRepository;
import talentcapitalme.com.comparatio.repository.CalculationResultRepository;
import talentcapitalme.com.comparatio.repository.EmployeeRepository;
import talentcapitalme.com.comparatio.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for super admin dashboard functionality
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService implements IDashboardService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final CalculationResultRepository calculationResultRepository;
    private final AdjustmentMatrixRepository matrixRepository;

    /**
     * Get dashboard data with pagination
     */
    public DashboardResponse getDashboard(int page, int size, String sortBy, String sortDir) {
        log.info("Fetching dashboard data for page: {}, size: {}", page, size);
        
        // Create pageable object
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get client admin users with pagination
        Page<User> clientUsers = userRepository.findByRole(UserRole.CLIENT_ADMIN, pageable);
        
        // Convert to ClientAccountSummary
        List<ClientAccountSummary> clientAccounts = clientUsers.getContent().stream()
                .map(this::enrichClientAccount)
                .collect(Collectors.toList());
        
        // Get dashboard statistics
        DashboardStats stats = getDashboardStats();
        
        // Build response
        return DashboardResponse.builder()
                .stats(stats)
                .clientAccounts(clientAccounts)
                .currentPage(clientUsers.getNumber())
                .totalPages(clientUsers.getTotalPages())
                .totalElements(clientUsers.getTotalElements())
                .hasNext(clientUsers.hasNext())
                .hasPrevious(clientUsers.hasPrevious())
                .build();
    }

    /**
     * Get all client accounts without pagination
     */
    public List<ClientAccountSummary> getAllClientAccounts() {
        log.info("Fetching all client accounts");
        
        List<User> clientUsers = userRepository.findByRoleAndActiveTrue(UserRole.CLIENT_ADMIN);
        
        return clientUsers.stream()
                .map(this::enrichClientAccount)
                .collect(Collectors.toList());
    }

    /**
     * Get client accounts with pagination
     */
    public ClientAccountsResponse getClientAccountsPaginated(int page, int size, String sortBy, String sortDir) {
        log.info("Fetching client accounts with pagination - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                page, size, sortBy, sortDir);
        
        // Create pageable object
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get client admin users with pagination
        Page<User> clientUsers = userRepository.findByRole(UserRole.CLIENT_ADMIN, pageable);
        
        // Convert to ClientAccountSummary
        List<ClientAccountSummary> clientAccounts = clientUsers.getContent().stream()
                .map(this::enrichClientAccount)
                .collect(Collectors.toList());
        
        // Build response
        return ClientAccountsResponse.builder()
                .clientAccounts(clientAccounts)
                .currentPage(clientUsers.getNumber())
                .totalPages(clientUsers.getTotalPages())
                .totalElements(clientUsers.getTotalElements())
                .hasNext(clientUsers.hasNext())
                .hasPrevious(clientUsers.hasPrevious())
                .build();
    }

    /**
     * Get client account by ID
     */
    public ClientAccountSummary getClientAccountById(String clientId) {
        log.info("Fetching client account by ID: {}", clientId);
        
        User clientUser = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        if (clientUser.getRole() != UserRole.CLIENT_ADMIN) {
            throw new RuntimeException("User is not a client admin");
        }
        
        return enrichClientAccount(clientUser);
    }

    /**
     * Toggle client account status (active/inactive)
     */
    public ClientAccountSummary toggleClientStatus(String clientId) {
        log.info("Toggling client status for ID: {}", clientId);
        
        User clientUser = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        if (clientUser.getRole() != UserRole.CLIENT_ADMIN) {
            throw new RuntimeException("User is not a client admin");
        }
        
        // Toggle active status
        boolean newStatus = !(clientUser.getActive() != null && clientUser.getActive());
        clientUser.setActive(newStatus);
        
        User savedUser = userRepository.save(clientUser);
        log.info("Client status toggled to: {}", newStatus);
        
        return enrichClientAccount(savedUser);
    }

    /**
     * Get dashboard statistics
     */
    private DashboardStats getDashboardStats() {
        log.debug("Calculating dashboard statistics");
        
        // Get all client admin users
        List<User> allClients = userRepository.findByRole(UserRole.CLIENT_ADMIN);
        
        int totalClients = allClients.size();
        int activeClients = (int) allClients.stream()
                .filter(user -> user.getActive() != null && user.getActive())
                .count();
        int inactiveClients = totalClients - activeClients;
        
        // Get total employees across all clients
        int totalEmployees = (int) employeeRepository.count();
        
        // Get total calculations
        int totalCalculations = (int) calculationResultRepository.count();
        
        // Get total matrices
        int totalMatrices = (int) matrixRepository.count();
        
        // Calculate average rating (placeholder - can be enhanced)
        double averageRating = 4.5; // Default average rating
        
        return DashboardStats.builder()
                .totalClients(totalClients)
                .activeClients(activeClients)
                .inactiveClients(inactiveClients)
                .totalEmployees(totalEmployees)
                .totalCalculations(totalCalculations)
                .totalMatrices(totalMatrices)
                .averageRating(averageRating)
                .lastUpdated(Instant.now().toString())
                .build();
    }

    /**
     * Enrich client account with additional data
     */
    private ClientAccountSummary enrichClientAccount(User user) {
        log.debug("Enriching client account for user: {}", user.getUsername());
        
        // Get employee count for this client
        int employeeCount = (int) employeeRepository.countByClientId(user.getId());
        
        // Get calculation count for this client
        int calculationCount = (int) calculationResultRepository.countByClientId(user.getId());
        
        // Calculate rating based on performance (placeholder logic)
        String rating = calculateRating(employeeCount, calculationCount);
        
        return ClientAccountSummary.builder()
                .id(user.getId())
                .companyName(user.getName())
                .contactPerson(user.getUsername())
                .email(user.getEmail())
                .industry("Technology") // Default industry
                .ratingScale(rating)
                .active(user.getActive() != null ? user.getActive() : false)
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getUpdatedAt())
                .totalEmployees(employeeCount)
                .totalCalculations(calculationCount)
                .status(user.getActive() != null && user.getActive() ? "Active" : "Inactive")
                .build();
    }

    /**
     * Calculate rating based on client activity
     */
    private String calculateRating(int employeeCount, int calculationCount) {
        // Simple rating calculation based on activity
        int score = 0;
        
        if (employeeCount > 0) score += 2;
        if (calculationCount > 0) score += 2;
        if (employeeCount > 10) score += 1;
        if (calculationCount > 50) score += 1;
        
        int rating = Math.min(5, Math.max(1, score));
        return rating + "/5";
    }
}
