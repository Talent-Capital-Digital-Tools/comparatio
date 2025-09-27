package talentcapitalme.com.comparatio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for dashboard statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    
    private int totalClients;
    private int activeClients;
    private int inactiveClients;
    private int totalEmployees;
    private int totalCalculations;
    private int totalMatrices;
    private double averageRating;
    private String lastUpdated;
}
