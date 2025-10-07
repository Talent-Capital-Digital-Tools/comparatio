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
import talentcapitalme.com.comparatio.dto.ClientDashboardStatistics;
import talentcapitalme.com.comparatio.dto.DashboardResponse;
import talentcapitalme.com.comparatio.dto.DashboardStats;
import talentcapitalme.com.comparatio.entity.CalculationResult;
import talentcapitalme.com.comparatio.entity.User;
import talentcapitalme.com.comparatio.enumeration.UserRole;
import talentcapitalme.com.comparatio.repository.AdjustmentMatrixRepository;
import talentcapitalme.com.comparatio.repository.CalculationResultRepository;
import talentcapitalme.com.comparatio.repository.EmployeeRepository;
import talentcapitalme.com.comparatio.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
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

    /**
     * Get comprehensive dashboard statistics for a specific client
     * Based on calculation results from bulk calculations stored in the database
     * 
     * @param clientId The client ID
     * @return ClientDashboardStatistics containing all analytics
     */
    @Override
    public ClientDashboardStatistics getClientDashboardStatistics(String clientId) {
        log.info("Fetching dashboard statistics for client: {}", clientId);
        
        // Fetch all calculation results for this client
        List<CalculationResult> results = calculationResultRepository.findByClientId(clientId, Pageable.unpaged()).getContent();
        
        // If no results found, return empty statistics
        if (results == null || results.isEmpty()) {
            log.warn("No calculation results found for client: {}", clientId);
            return buildEmptyStatistics(clientId);
        }
        
        log.info("Found {} calculation results for client: {}", results.size(), clientId);
        
        // Calculate basic metrics
        int totalEmployees = results.size();
        BigDecimal totalCurrentSalary = results.stream()
                .map(CalculationResult::getCurrentSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalNewSalary = results.stream()
                .map(CalculationResult::getNewSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate total percentage change: ((totalNew - totalOld) / totalOld) * 100
        BigDecimal totalPercentageChange = BigDecimal.ZERO;
        if (totalCurrentSalary.compareTo(BigDecimal.ZERO) > 0) {
            totalPercentageChange = totalNewSalary.subtract(totalCurrentSalary)
                    .divide(totalCurrentSalary, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        
        // Calculate Percentage Increase Analysis
        ClientDashboardStatistics.PercentageIncreaseAnalysis percentageIncreaseAnalysis = calculatePercentageIncreaseAnalysis(results);
        
        // Calculate Amount Increase Analysis
        ClientDashboardStatistics.AmountIncreaseAnalysis amountIncreaseAnalysis = calculateAmountIncreaseAnalysis(results);
        
        // Build and return the response
        return ClientDashboardStatistics.builder()
                .clientId(clientId)
                .totalEmployees(totalEmployees)
                .totalCurrentSalary(totalCurrentSalary.setScale(2, RoundingMode.HALF_UP))
                .totalNewSalary(totalNewSalary.setScale(2, RoundingMode.HALF_UP))
                .totalPercentageChange(totalPercentageChange)
                .percentageIncreaseAnalysis(percentageIncreaseAnalysis)
                .amountIncreaseAnalysis(amountIncreaseAnalysis)
                .lastUpdated(Instant.now().toString())
                .build();
    }
    
    
    /**
     * Calculate Percentage Increase Analysis (min, max, average)
     */
    private ClientDashboardStatistics.PercentageIncreaseAnalysis calculatePercentageIncreaseAnalysis(List<CalculationResult> results) {
        // Get all percentage increases (from increasePct field), excluding zero values
        List<BigDecimal> percentageIncreases = results.stream()
                .map(CalculationResult::getIncreasePct)
                .filter(pct -> pct != null && pct.compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
        
        if (percentageIncreases.isEmpty()) {
            return ClientDashboardStatistics.PercentageIncreaseAnalysis.builder()
                    .minimum(BigDecimal.ZERO)
                    .maximum(BigDecimal.ZERO)
                    .average(BigDecimal.ZERO)
                    .build();
        }
        
        BigDecimal minimum = percentageIncreases.stream()
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        
        BigDecimal maximum = percentageIncreases.stream()
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        
        BigDecimal sum = percentageIncreases.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal average = sum.divide(BigDecimal.valueOf(percentageIncreases.size()), 2, RoundingMode.HALF_UP);
        
        return ClientDashboardStatistics.PercentageIncreaseAnalysis.builder()
                .minimum(minimum.setScale(2, RoundingMode.HALF_UP))
                .maximum(maximum.setScale(2, RoundingMode.HALF_UP))
                .average(average)
                .build();
    }
    
    /**
     * Calculate Amount Increase Analysis (min, max, average)
     */
    private ClientDashboardStatistics.AmountIncreaseAnalysis calculateAmountIncreaseAnalysis(List<CalculationResult> results) {
        // Calculate amount increase for each result: newSalary - currentSalary, excluding zero values
        List<BigDecimal> amountIncreases = results.stream()
                .filter(r -> r.getCurrentSalary() != null && r.getNewSalary() != null)
                .map(r -> r.getNewSalary().subtract(r.getCurrentSalary()))
                .filter(amount -> amount.compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
        
        if (amountIncreases.isEmpty()) {
            return ClientDashboardStatistics.AmountIncreaseAnalysis.builder()
                    .minimum(BigDecimal.ZERO)
                    .maximum(BigDecimal.ZERO)
                    .average(BigDecimal.ZERO)
                    .build();
        }
        
        BigDecimal minimum = amountIncreases.stream()
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        
        BigDecimal maximum = amountIncreases.stream()
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        
        BigDecimal sum = amountIncreases.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal average = sum.divide(BigDecimal.valueOf(amountIncreases.size()), 2, RoundingMode.HALF_UP);
        
        return ClientDashboardStatistics.AmountIncreaseAnalysis.builder()
                .minimum(minimum.setScale(2, RoundingMode.HALF_UP))
                .maximum(maximum.setScale(2, RoundingMode.HALF_UP))
                .average(average)
                .build();
    }
    
    /**
     * Build empty statistics when no calculation results are found
     */
    private ClientDashboardStatistics buildEmptyStatistics(String clientId) {
        return ClientDashboardStatistics.builder()
                .clientId(clientId)
                .totalEmployees(0)
                .totalCurrentSalary(BigDecimal.ZERO)
                .totalNewSalary(BigDecimal.ZERO)
                .totalPercentageChange(BigDecimal.ZERO)
                .percentageIncreaseAnalysis(ClientDashboardStatistics.PercentageIncreaseAnalysis.builder()
                        .minimum(BigDecimal.ZERO)
                        .maximum(BigDecimal.ZERO)
                        .average(BigDecimal.ZERO)
                        .build())
                .amountIncreaseAnalysis(ClientDashboardStatistics.AmountIncreaseAnalysis.builder()
                        .minimum(BigDecimal.ZERO)
                        .maximum(BigDecimal.ZERO)
                        .average(BigDecimal.ZERO)
                        .build())
                .lastUpdated(Instant.now().toString())
                .build();
    }
}
