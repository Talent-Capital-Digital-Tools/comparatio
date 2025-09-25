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
public class MatrixValidationResult {
    private boolean isValid;
    private List<String> errors;
    private List<String> warnings;
    private String summary;
    private int totalMatrices;
    
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
    
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }
    
    public int getErrorCount() {
        return errors != null ? errors.size() : 0;
    }
    
    public int getWarningCount() {
        return warnings != null ? warnings.size() : 0;
    }
}
