package talentcapitalme.com.comparatio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BulkResponse {
    private String batchId;
    private int totalRows;
    private int successCount;
    private int errorCount;
    private List<BulkRowResult> rows;
}
