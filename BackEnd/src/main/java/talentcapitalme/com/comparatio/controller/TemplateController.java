package talentcapitalme.com.comparatio.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
public class TemplateController {
    @Value("${app.template.path}")
    private Resource template;

    @GetMapping
    public ResponseEntity<Resource> download() {
        log.info("Template Controller: Processing template download request");
        log.info("Template Controller: Serving template file: compa_template.csv");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=compa_template.csv")
                .body(template);
    }
}
