package talentcapitalme.com.comparatio.validation;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import talentcapitalme.com.comparatio.dto.MatrixUpdateRequest;

import java.math.BigDecimal;

/**
 * Custom validator for matrix update requests
 * Validates business rules and constraints for adjustment matrices
 */
@Slf4j
@Component
public class MatrixValidator implements Validator {

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return MatrixUpdateRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        MatrixUpdateRequest request = (MatrixUpdateRequest) target;
        
        // Validate performance bucket
        if (request.getPerfBucket() == null || request.getPerfBucket() < 1 || request.getPerfBucket() > 3) {
            errors.rejectValue("perfBucket", "invalid.perfBucket", 
                    "Performance bucket must be between 1 and 3");
        }
        
        // Validate compa ratio range
        if (request.getCompaFrom() == null || request.getCompaTo() == null) {
            errors.rejectValue("compaFrom", "invalid.compaRange", 
                    "Compa ratio range is required");
        } else {
            if (request.getCompaFrom().compareTo(BigDecimal.ZERO) < 0) {
                errors.rejectValue("compaFrom", "invalid.compaFrom", 
                        "Compa ratio from must be non-negative");
            }
            
            if (request.getCompaTo().compareTo(request.getCompaFrom()) <= 0) {
                errors.rejectValue("compaTo", "invalid.compaTo", 
                        "Compa ratio to must be greater than compa ratio from");
            }
            
            if (request.getCompaTo().compareTo(BigDecimal.valueOf(10)) > 0) {
                errors.rejectValue("compaTo", "invalid.compaTo", 
                        "Compa ratio to must not exceed 10.0");
            }
        }
        
        // Validate percentage values
        validatePercentage(request.getPctLt5Years(), "pctLt5Years", errors);
        validatePercentage(request.getPctGte5Years(), "pctGte5Years", errors);
        
        // Business logic validation
        validateBusinessLogic(request, errors);
    }
    
    private void validatePercentage(BigDecimal percentage, String fieldName, Errors errors) {
        if (percentage == null) {
            errors.rejectValue(fieldName, "required.percentage", 
                    "Percentage is required");
            return;
        }
        
        if (percentage.compareTo(BigDecimal.ZERO) < 0) {
            errors.rejectValue(fieldName, "invalid.percentage.negative", 
                    "Percentage cannot be negative");
        }
        
        if (percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            errors.rejectValue(fieldName, "invalid.percentage.exceeds", 
                    "Percentage cannot exceed 100%");
        }
    }
    
    private void validateBusinessLogic(MatrixUpdateRequest request, Errors errors) {
        // Validate that higher performance buckets generally have higher percentages
        if (request.getPerfBucket() != null && 
            request.getPctLt5Years() != null && 
            request.getPctGte5Years() != null) {
            
            // Check for reasonable percentage ranges based on performance bucket
            BigDecimal maxPercentage = getMaxPercentageForBucket(request.getPerfBucket());
            
            if (request.getPctLt5Years().compareTo(maxPercentage) > 0) {
                errors.rejectValue("pctLt5Years", "invalid.percentage.bucket", 
                        String.format("Percentage for bucket %d should not exceed %s%%", 
                                request.getPerfBucket(), maxPercentage));
            }
            
            if (request.getPctGte5Years().compareTo(maxPercentage) > 0) {
                errors.rejectValue("pctGte5Years", "invalid.percentage.bucket", 
                        String.format("Percentage for bucket %d should not exceed %s%%", 
                                request.getPerfBucket(), maxPercentage));
            }
        }
    }
    
    private BigDecimal getMaxPercentageForBucket(int bucket) {
        switch (bucket) {
            case 1: return BigDecimal.valueOf(15); // Partially meets - lower percentages
            case 2: return BigDecimal.valueOf(25); // Meets targets - moderate percentages
            case 3: return BigDecimal.valueOf(35); // Exceeds targets - higher percentages
            default: return BigDecimal.valueOf(100);
        }
    }
}
