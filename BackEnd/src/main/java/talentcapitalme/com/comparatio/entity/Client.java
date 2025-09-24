package talentcapitalme.com.comparatio.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("clients")
public class Client {
    @Id
    private String id;            // e.g., "talenetCapital"
    @Indexed(unique = true)
    private String name;          // company name
    private Boolean active;       // enable/disable tenant
}