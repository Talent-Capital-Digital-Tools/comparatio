package talentcapitalme.com.comparatio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import talentcapitalme.com.comparatio.entity.AdjustmentMatrix;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatrixResponse {
    private String id;
    private String clientId;
    private String clientName;
    private Integer perfBucket;
    private BigDecimal compaFrom;
    private BigDecimal compaTo;
    private BigDecimal pctLt5Years;
    private BigDecimal pctGte5Years;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean active;
    private String compaRangeLabel;
    
    public static MatrixResponse fromEntity(AdjustmentMatrix matrix, String clientName) {
        return MatrixResponse.builder()
                .id(matrix.getId())
                .clientId(matrix.getClientId())
                .clientName(clientName)
                .perfBucket(matrix.getPerfBucket())
                .compaFrom(matrix.getCompaFrom())
                .compaTo(matrix.getCompaTo())
                .pctLt5Years(matrix.getPctLt5Years())
                .pctGte5Years(matrix.getPctGte5Years())
                .effectiveFrom(matrix.getEffectiveFrom())
                .effectiveTo(matrix.getEffectiveTo())
                .active(matrix.getActive())
                .compaRangeLabel(createCompaRangeLabel(matrix))
                .build();
    }
    
    private static String createCompaRangeLabel(AdjustmentMatrix matrix) {
        BigDecimal from = matrix.getCompaFrom().multiply(BigDecimal.valueOf(100));
        BigDecimal to = matrix.getCompaTo().multiply(BigDecimal.valueOf(100));
        boolean open = matrix.getCompaTo().compareTo(BigDecimal.valueOf(9.99)) >= 0;
        return open ? from.stripTrailingZeros().toPlainString() + "%+"
                : from.stripTrailingZeros().toPlainString() + "%–" + 
                  to.stripTrailingZeros().toPlainString() + "%";
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class MatrixBulkResponse {
    private String clientId;
    private String clientName;
    private int totalMatrices;
    private int performanceBucket1Count;
    private int performanceBucket2Count;
    private int performanceBucket3Count;
    private List<MatrixResponse> matrices;
}
