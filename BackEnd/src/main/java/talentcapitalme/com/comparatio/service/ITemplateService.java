package talentcapitalme.com.comparatio.service;

import java.io.IOException;

/**
 * Interface for Template Service operations
 */
public interface ITemplateService {
    
    /**
     * Generate Excel template for bulk compensation calculation upload
     */
    byte[] generateBulkUploadTemplate() throws IOException;
}
