package talentcapitalme.com.comparatio.service;

import talentcapitalme.com.comparatio.dto.CalcRequest;
import talentcapitalme.com.comparatio.dto.CalcResponse;

/**
 * Interface for Compensation Service operations
 */
public interface ICompensationService {
    
    /**
     * Calculate compensation for individual employee
     */
    CalcResponse calculate(CalcRequest req);
}
