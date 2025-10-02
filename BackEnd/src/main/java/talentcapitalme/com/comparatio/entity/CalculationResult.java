package talentcapitalme.com.comparatio.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document("calculation_results")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculationResult extends Audit {
    @Id
    private String id;
    private String clientId;
    private String batchId;         // for bulk uploads
    private String employeeCode;
    private String employeeName;    // Added employee name field
    private String jobTitle;
    private Integer yearsExperience;
    private Integer perfBucket;
    private BigDecimal currentSalary;
    private BigDecimal midOfScale;
    private BigDecimal compaRatio;
    private String compaLabel;
    private BigDecimal increasePct;
    private BigDecimal newSalary;

}

