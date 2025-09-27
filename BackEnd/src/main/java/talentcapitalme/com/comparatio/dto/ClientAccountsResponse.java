package talentcapitalme.com.comparatio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for paginated client accounts response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientAccountsResponse {
    
    private List<ClientAccountSummary> clientAccounts;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;
    private boolean hasPrevious;
}
