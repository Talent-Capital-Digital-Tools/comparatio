package talentcapitalme.com.comparatio.service;

import org.springframework.web.multipart.MultipartFile;
import talentcapitalme.com.comparatio.dto.BulkResponse;
import talentcapitalme.com.comparatio.dto.BulkRowResult;

import java.io.IOException;
import java.util.List;

/**
 * Interface for Excel Processing Service operations
 */
public interface IExcelProcessingService {
    
    /**
     * Process Excel file with comprehensive validation and error handling
     */
    BulkResponse processExcelFile(MultipartFile file) throws IOException;
    
    /**
     * Generate enhanced Excel file
     */
    byte[] generateEnhancedExcel(List<BulkRowResult> results, String filename) throws IOException;
}
