package talentcapitalme.com.comparatio.entity;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
@Document("calculation_results")
@Builder
public class CalculationResult extends  Audit{
    @Id
    private String id;
    private String clientId;
    private String batchId;         // for bulk uploads
    private String employeeCode;
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

