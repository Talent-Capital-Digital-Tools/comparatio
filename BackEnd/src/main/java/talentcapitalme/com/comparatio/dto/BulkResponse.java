package talentcapitalme.com.comparatio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkResponse {
    private String batchId;
    private int totalRows;
    private int successCount;
    private int errorCount;
    private List<BulkRowResult> rows;

    // Pagination metadata (optional - only populated for pageable endpoints)
    private Integer pageNumber;
    private Integer pageSize;
    private Integer totalPages;
    private Long totalElements;
    private Boolean first;
    private Boolean last;

    // Constructor for backward compatibility (without pagination)
    public BulkResponse(String batchId, int totalRows, int successCount, int errorCount, List<BulkRowResult> rows) {
        this.batchId = batchId;
        this.totalRows = totalRows;
        this.successCount = successCount;
        this.errorCount = errorCount;
        this.rows = rows;
    }
}
