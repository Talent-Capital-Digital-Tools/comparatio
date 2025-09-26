package talentcapitalme.com.comparatio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import talentcapitalme.com.comparatio.service.TemplateService;

import java.io.IOException;

/**
 * Template Controller
 * 
 * Purpose: Handles template file download operations
 * - Excel template download for bulk uploads
 * - Template file management and distribution
 * - Standardized file format for data import
 * - User-friendly template access for data preparation
 */
@Slf4j
@RestController
@RequestMapping("/api/template")
@RequiredArgsConstructor
@Tag(name = "Template Management", description = "Excel template download for bulk operations")
public class TemplateController {
    
    private final TemplateService templateService;

    @Operation(summary = "Download Bulk Upload Template", 
               description = "Download Excel template with pre-set columns for bulk compensation calculation upload")
    @GetMapping("/bulk-upload")
    public ResponseEntity<byte[]> downloadBulkUploadTemplate() {
        log.info("Template Controller: Processing bulk upload template download request");
        
        try {
            byte[] templateData = templateService.generateBulkUploadTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "compensation_upload_template.xlsx");
            
            log.info("Template Controller: Template generated successfully ({} bytes)", templateData.length);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(templateData);
                    
        } catch (IOException e) {
            log.error("Template Controller: Error generating template: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
