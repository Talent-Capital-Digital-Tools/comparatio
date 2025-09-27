package talentcapitalme.com.comparatio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CalcResponse {
    private BigDecimal compaRatio; // 0.8333
    private String compaLabel; // "71%–85%"
    private BigDecimal increasePct; // 17
    private BigDecimal newSalary; // 11700.00
}