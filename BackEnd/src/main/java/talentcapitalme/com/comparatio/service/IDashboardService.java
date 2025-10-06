package talentcapitalme.com.comparatio.service;

import talentcapitalme.com.comparatio.dto.ClientAccountSummary;
import talentcapitalme.com.comparatio.dto.ClientAccountsResponse;
import talentcapitalme.com.comparatio.dto.ClientDashboardStatistics;
import talentcapitalme.com.comparatio.dto.DashboardResponse;

import java.util.List;

/**
 * Interface for Dashboard Service operations
 */
public interface IDashboardService {
    
    /**
     * Get dashboard data with pagination (Super Admin)
     */
    DashboardResponse getDashboard(int page, int size, String sortBy, String sortDir);
    
    /**
     * Get all client accounts without pagination (Super Admin)
     */
    List<ClientAccountSummary> getAllClientAccounts();
    
    /**
     * Get client accounts with pagination (Super Admin)
     */
    ClientAccountsResponse getClientAccountsPaginated(int page, int size, String sortBy, String sortDir);
    
    /**
     * Get client account by ID (Super Admin)
     */
    ClientAccountSummary getClientAccountById(String clientId);
    
    /**
     * Toggle client account status (active/inactive) (Super Admin)
     */
    ClientAccountSummary toggleClientStatus(String clientId);
    
    /**
     * Get comprehensive dashboard statistics for a specific client
     * Based on calculation results from bulk calculations
     * 
     * @param clientId The client ID
     * @return ClientDashboardStatistics containing all analytics
     */
    ClientDashboardStatistics getClientDashboardStatistics(String clientId);
}
