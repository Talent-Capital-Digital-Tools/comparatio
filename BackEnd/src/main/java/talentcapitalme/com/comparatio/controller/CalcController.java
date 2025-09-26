package talentcapitalme.com.comparatio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import talentcapitalme.com.comparatio.dto.BulkResponse;
import talentcapitalme.com.comparatio.dto.BulkRowResult;
import talentcapitalme.com.comparatio.dto.CalcRequest;
import talentcapitalme.com.comparatio.dto.CalcResponse;
import talentcapitalme.com.comparatio.repository.CalculationResultRepository;
import talentcapitalme.com.comparatio.security.Authz;
import talentcapitalme.com.comparatio.service.OptimizedBulkService;
import talentcapitalme.com.comparatio.service.CompensationService;

import java.io.IOException;

/**
 * Calculation Controller
 * 
 * Purpose: Handles compensation calculation operations
 * - Individual employee salary calculations
 * - Bulk Excel file processing with multithreading
 * - Calculation result retrieval and download
 * - Performance-optimized processing for large datasets
 */
@Slf4j
@RestController
@RequestMapping("/api/calc")
@RequiredArgsConstructor
@Tag(name = "Calculations", description = "Individual and bulk compensation calculations")
public class CalcController {
    private final CompensationService service;
    private final OptimizedBulkService bulkService;
    private final CalculationResultRepository resultRepo;

    @Operation(summary = "Individual Calculation", description = "Calculate compensation for a single employee")
    @PostMapping("/individual")
    public CalcResponse calc(@Valid @RequestBody CalcRequest req) { 
        log.info("Calculation Controller: Processing individual calculation for employee: {}", req.getEmployeeCode());
        CalcResponse response = service.calculate(req);
        log.info("Calculation Controller: Individual calculation completed for employee: {} with new salary: {}", 
                req.getEmployeeCode(), response.getNewSalary());
        return response; 
    }

    @Operation(summary = "Bulk Calculation", description = "Process Excel file with multiple employee calculations")
    @PostMapping(value="/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BulkResponse bulk(@Parameter(description = "Excel file with employee data") @RequestPart("file") MultipartFile file) throws IOException { 
        log.info("Calculation Controller: Processing bulk Excel file upload: {} ({} bytes)", 
                file.getOriginalFilename(), file.getSize());
        BulkResponse response = bulkService.process(file);
        log.info("Calculation Controller: Bulk processing completed - Total: {}, Success: {}, Errors: {}", 
                response.getTotalRows(), response.getSuccessCount(), response.getErrorCount());
        return response; 
    }

    @Operation(summary = "Download Results", description = "Download Excel file with calculation results for a batch")
    @GetMapping("/bulk/{batchId}")
    public ResponseEntity<byte[]> download(@Parameter(description = "Batch ID from bulk calculation") @PathVariable String batchId) throws IOException {
        log.info("Calculation Controller: Processing download request for batch: {}", batchId);
        String clientId = Authz.getCurrentUserClientId();
        var rows = resultRepo.findByBatchId(batchId).stream()
                .filter(r -> clientId.equals(r.getClientId()))
                .map(r -> BulkRowResult.builder()
                        .employeeCode(r.getEmployeeCode())
                        .jobTitle(r.getJobTitle())
                        .yearsExperience(r.getYearsExperience())
                        .performanceRating5(r.getPerfBucket()==3?4: r.getPerfBucket()==2?3:1)
                        .currentSalary(r.getCurrentSalary())
                        .midOfScale(r.getMidOfScale())
                        .compaRatio(r.getCompaRatio())
                        .compaLabel(r.getCompaLabel())
                        .increasePct(r.getIncreasePct())
                        .newSalary(r.getNewSalary())
                        .build()).toList();

        log.info("Calculation Controller: Found {} calculation results for batch: {}", rows.size(), batchId);
        byte[] xlsx = bulkService.exportExcel(rows);
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        h.setContentDisposition(ContentDisposition.attachment().filename("bulk-results-"+batchId+".xlsx").build());
        log.info("Calculation Controller: Excel file generated successfully for batch: {} ({} bytes)", batchId, xlsx.length);
        return new ResponseEntity<>(xlsx, h, HttpStatus.OK);
    }
}
