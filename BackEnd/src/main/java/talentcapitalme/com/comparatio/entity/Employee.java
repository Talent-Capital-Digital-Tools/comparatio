package talentcapitalme.com.comparatio.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

/**
    An employee record (for storing in DB instead of Excel).
 **/



@Document("employees")
public class Employee {
    @Id
    private String id;
    private String clientId;
    private String name;
    private String jobTitle;
    private String jobGradeId;
    private Integer yearsExperience;
    private BigDecimal currentSalary;
    private Integer performanceRating5;  // 1–5
}