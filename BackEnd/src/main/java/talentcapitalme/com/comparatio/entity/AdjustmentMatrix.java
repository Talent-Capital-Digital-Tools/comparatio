package talentcapitalme.com.comparatio.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("adjustment_matrix")
public class AdjustmentMatrix extends Audit {
    @Id
    private String id;                 // e.g., m_acme_3_0.71_0.85
    private String clientId;           // tenant owner

    private Integer perfBucket;        // 1, 2, 3  (derived from rating 1..5)
    private BigDecimal compaFrom;      // inclusive (e.g., 0.71)
    private BigDecimal compaTo;        // exclusive (e.g., 0.85, or 9.99 = 130%+)

    private BigDecimal pctLt5Years;    // % if < 5 yrs (e.g., 17)
    private BigDecimal pctGte5Years;   // % if >= 5 yrs (e.g., 21)

    // optional time-based control
    private LocalDate effectiveFrom;   // null = open
    private LocalDate effectiveTo;     // null = open
    private Boolean active;            // mark old rows inactive instead of delete         // whether this row is still valid

}