package talentcapitalme.com.comparatio.service;

import talentcapitalme.com.comparatio.dto.ClientAccountSummary;
import talentcapitalme.com.comparatio.dto.ClientAccountsResponse;
import talentcapitalme.com.comparatio.dto.DashboardResponse;

import java.util.List;

/**
 * Interface for Dashboard Service operations
 */
public interface IDashboardService {
    
    /**
     * Get dashboard data with pagination
     */
    DashboardResponse getDashboard(int page, int size, String sortBy, String sortDir);
    
    /**
     * Get all client accounts without pagination
     */
    List<ClientAccountSummary> getAllClientAccounts();
    
    /**
     * Get client accounts with pagination
     */
    ClientAccountsResponse getClientAccountsPaginated(int page, int size, String sortBy, String sortDir);
    
    /**
     * Get client account by ID
     */
    ClientAccountSummary getClientAccountById(String clientId);
    
    /**
     * Toggle client account status (active/inactive)
     */
    ClientAccountSummary toggleClientStatus(String clientId);
}
