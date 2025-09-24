package talentcapitalme.com.comparatio.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

/**
 * JobGrade entity representing a job grade within a company.
 * Each job grade has a unique identifier, title, and midpoint of scale.
 */


@Document("job_grades")
public class JobGrade extends Audit {
    @Id
    private String id;
    private String clientId;      // which company this belongs to
    private String title;         // "Manager"
    private BigDecimal midOfScale;//  12000.00 (the midpoint)
}