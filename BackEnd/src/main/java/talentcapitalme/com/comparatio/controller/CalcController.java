package talentcapitalme.com.comparatio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import talentcapitalme.com.comparatio.entity.CalculationResult;
import talentcapitalme.com.comparatio.repository.CalculationResultRepository;
import talentcapitalme.com.comparatio.security.Authz;
import talentcapitalme.com.comparatio.service.IExcelProcessingService;
import talentcapitalme.com.comparatio.service.ICompensationService;
import talentcapitalme.com.comparatio.util.CalculationResultMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private final ICompensationService service;
    private final IExcelProcessingService excelProcessingService;
    private final CalculationResultRepository resultRepo;
    private final CalculationResultMapper resultMapper;

    @Operation(summary = "Individual Calculation", description = "Calculate compensation for a single employee")
    @PostMapping("/individual")
    public CalcResponse calc(@Valid @RequestBody CalcRequest req) {
        log.info("Calculation Controller: Processing individual calculation for employee: {}", req.getEmployeeCode());
        CalcResponse response = service.calculate(req);
        log.info("Calculation Controller: Individual calculation completed for employee: {} with new salary: {}",
                req.getEmployeeCode(), response.getNewSalary());
        return response;
    }

    @Operation(summary = "Bulk Calculation", description = "Process Excel file and return enhanced Excel with calculation results")
    @PostMapping(value = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> bulk(
            @Parameter(description = "Excel file with employee data") @RequestParam("file") MultipartFile file) {
        log.info("Calculation Controller: Processing bulk Excel file upload: {} ({} bytes)",
                file.getOriginalFilename(), file.getSize());

        try {
            // Process the file using the new Excel processing service
            BulkResponse response = excelProcessingService.processExcelFile(file);
            log.info("Calculation Controller: Bulk processing completed - Total: {}, Success: {}, Errors: {}",
                    response.getTotalRows(), response.getSuccessCount(), response.getErrorCount());

            // Generate enhanced Excel file with results
            byte[] xlsx = excelProcessingService.generateEnhancedExcel(response.getRows(), response.getBatchId());

            // Set up response headers for Excel download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("bulk-calculation-results-" + response.getBatchId() + ".xlsx").build());

            log.info("Calculation Controller: Excel file generated successfully for batch: {} ({} bytes)",
                    response.getBatchId(), xlsx.length);
            return new ResponseEntity<>(xlsx, headers, HttpStatus.OK);

        } catch (IOException e) {
            log.error("Calculation Controller: Error processing Excel file: {}", e.getMessage(), e);

            // Return a helpful error response
            String errorMessage = e.getMessage();
            if (errorMessage.contains("YearOfEra") || errorMessage.contains("date")) {
                errorMessage = "The Excel file contains unsupported date formats. Please convert all date columns to text format before uploading.";
            } else if (errorMessage.contains("Unsupported")) {
                errorMessage = "The Excel file contains unsupported formatting. Please save as a simple .xlsx format with only text and numbers.";
            }

            // Create an error response as JSON
            String errorJson = String.format(
                    "{\"error\": \"%s\", \"suggestion\": \"Please ensure your Excel file has these columns: Employee Code, Employee Name, Job Title, Years of Experience, Performance Rating, Current Salary, Mid of Scale\"}",
                    errorMessage.replace("\"", "\\\""));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(errorJson.getBytes(), headers, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Calculation Controller: Unexpected error processing Excel file: {}", e.getMessage(), e);

            String errorJson = String.format("{\"error\": \"Unexpected error: %s\"}",
                    e.getMessage().replace("\"", "\\\""));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(errorJson.getBytes(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Upload Excel File", description = "Simple file upload endpoint for testing")
    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded!");
        }
        return ResponseEntity.ok("File uploaded: " + file.getOriginalFilename());
    }

    @Operation(summary = "Get Calculation Results as Table Data", description = "Get paginated calculation results for a batch as JSON for table display")
    @GetMapping("/bulk/{batchId}/table")
    public ResponseEntity<BulkResponse> getResultsAsTable(
            @Parameter(description = "Batch ID from bulk calculation") @PathVariable String batchId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size) {
        log.info("Calculation Controller: Processing paginated table data request for batch: {} (page: {}, size: {})",
                batchId, page, size);
        String clientId = Authz.getCurrentUserClientId();

        // Get all results first to calculate totals with proper row indexing
        List<CalculationResult> dbResults = resultRepo.findByBatchId(batchId).stream()
                .filter(r -> clientId.equals(r.getClientId()))
                .sorted((r1, r2) -> r1.getEmployeeCode().compareTo(r2.getEmployeeCode())) // Sort by employee code for consistent ordering
                .toList();
        
        // Convert to BulkRowResult with proper row indexing (starting from 1)
        var allRows = new ArrayList<BulkRowResult>();
        for (int i = 0; i < dbResults.size(); i++) {
            CalculationResult r = dbResults.get(i);
            BulkRowResult rowResult = BulkRowResult.builder()
                    .rowIndex(i + 1) // Excel rows start from 1, ensures no missing first row
                    .employeeCode(r.getEmployeeCode())
                    .employeeName(r.getEmployeeName() != null ? r.getEmployeeName() : "N/A")
                    .jobTitle(r.getJobTitle())
                    .yearsExperience(r.getYearsExperience())
                    .performanceRating5(r.getPerfBucket() == 3 ? 5 : r.getPerfBucket() == 2 ? 3 : 2)
                    .currentSalary(r.getCurrentSalary())
                    .midOfScale(r.getMidOfScale())
                    .compaRatio(r.getCompaRatio())
                    .compaLabel(r.getCompaLabel())
                    .increasePct(r.getIncreasePct())
                    .newSalary(r.getNewSalary())
                    .increaseAmount(r.getNewSalary().subtract(r.getCurrentSalary()))
                    .build();
            allRows.add(rowResult);
        }

        int successCount = (int) allRows.stream().filter(r -> r.getError() == null).count();
        int errorCount = allRows.size() - successCount;

        // Apply pagination
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, allRows.size());
        var paginatedRows = allRows.subList(startIndex, endIndex);

        BulkResponse response = new BulkResponse(batchId, allRows.size(), successCount, errorCount, paginatedRows);

        log.info(
                "Calculation Controller: Found {} calculation results for batch: {} (Success: {}, Errors: {}) - Page: {}/{}",
                allRows.size(), batchId, successCount, errorCount, page + 1,
                (int) Math.ceil((double) allRows.size() / size));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Download Results", description = "Download Excel file with calculation results for a batch")
    @GetMapping("/bulk/{batchId}")
    public ResponseEntity<byte[]> download(
            @Parameter(description = "Batch ID from bulk calculation") @PathVariable String batchId)
            throws IOException {
        log.info("Calculation Controller: Processing download request for batch: {}", batchId);
        String clientId = Authz.getCurrentUserClientId();
        
        // Get results with proper sorting and indexing
        List<CalculationResult> dbResults = resultRepo.findByBatchId(batchId).stream()
                .filter(r -> clientId.equals(r.getClientId()))
                .sorted((r1, r2) -> r1.getEmployeeCode().compareTo(r2.getEmployeeCode()))
                .toList();
        
        // Convert to BulkRowResult with proper row indexing
        var rows = new ArrayList<BulkRowResult>();
        for (int i = 0; i < dbResults.size(); i++) {
            CalculationResult r = dbResults.get(i);
            BulkRowResult rowResult = BulkRowResult.builder()
                    .rowIndex(i + 1) // Excel rows start from 1
                    .employeeCode(r.getEmployeeCode())
                    .employeeName(r.getEmployeeName() != null ? r.getEmployeeName() : "N/A")
                    .jobTitle(r.getJobTitle())
                    .yearsExperience(r.getYearsExperience())
                    .performanceRating5(r.getPerfBucket() == 3 ? 5 : r.getPerfBucket() == 2 ? 3 : 2)
                    .currentSalary(r.getCurrentSalary())
                    .midOfScale(r.getMidOfScale())
                    .compaRatio(r.getCompaRatio())
                    .compaLabel(r.getCompaLabel())
                    .increasePct(r.getIncreasePct())
                    .newSalary(r.getNewSalary())
                    .increaseAmount(r.getNewSalary().subtract(r.getCurrentSalary()))
                    .build();
            rows.add(rowResult);
        }

        log.info("Calculation Controller: Found {} calculation results for batch: {}", rows.size(), batchId);
        byte[] xlsx = excelProcessingService.generateEnhancedExcel(rows, batchId);
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        h.setContentDisposition(ContentDisposition.attachment().filename("bulk-results-" + batchId + ".xlsx").build());
        log.info("Calculation Controller: Excel file generated successfully for batch: {} ({} bytes)", batchId,
                xlsx.length);
        return new ResponseEntity<>(xlsx, h, HttpStatus.OK);
    }

    @Operation(summary = "Get All Calculation Results (Pageable)", description = "Fetch all calculation results for current client with Spring Data pagination and sorting")
    @GetMapping("/results")
    public ResponseEntity<BulkResponse> getAllResults(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sort by field (createdAt, employeeCode, newSalary, etc.)") @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction (ASC or DESC)") @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Fetching all calculation results - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, sortDirection);

        String clientId = Authz.getCurrentUserClientId();

        // Create pageable with sorting
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // Fetch page from database (efficient - only loads requested page!)
        Page<CalculationResult> resultPage = resultRepo.findByClientId(clientId, pageable);

        // Convert to BulkRowResult DTOs using utility mapper
        List<BulkRowResult> rows = resultMapper.convertToBulkRowResults(resultPage.getContent());

        // Build response with pagination metadata
        BulkResponse response = BulkResponse.builder()
                .batchId("all")
                .totalRows((int) resultPage.getTotalElements())
                .successCount(rows.size())
                .errorCount(0)
                .rows(rows)
                .pageNumber(resultPage.getNumber())
                .pageSize(resultPage.getSize())
                .totalPages(resultPage.getTotalPages())
                .totalElements(resultPage.getTotalElements())
                .first(resultPage.isFirst())
                .last(resultPage.isLast())
                .build();

        log.info("Retrieved {} calculation results (total: {}, page {}/{})",
                rows.size(), resultPage.getTotalElements(), page + 1, resultPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Calculation Results by Batch and Client (Pageable)", description = "Fetch calculation results for a specific batch and client with efficient pagination")
    @GetMapping("/results/batch/{batchId}")
    public ResponseEntity<BulkResponse> getResultsByBatch(
            @Parameter(description = "Batch ID") @PathVariable String batchId,

            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction (ASC or DESC)") @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Fetching results for batch: {} - page: {}, size: {}", batchId, page, size);

        String clientId = Authz.getCurrentUserClientId();

        // Create pageable with sorting
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // Fetch page from database (efficient - only loads requested page!)
        Page<CalculationResult> resultPage = resultRepo.findByClientIdAndBatchId(clientId, batchId, pageable);

        // Convert to BulkRowResult DTOs using utility mapper
        List<BulkRowResult> rows = resultMapper.convertToBulkRowResults(resultPage.getContent());

        // Build response with pagination metadata
        BulkResponse response = BulkResponse.builder()
                .batchId(batchId)
                .totalRows((int) resultPage.getTotalElements())
                .successCount(rows.size())
                .errorCount(0)
                .rows(rows)
                .pageNumber(resultPage.getNumber())
                .pageSize(resultPage.getSize())
                .totalPages(resultPage.getTotalPages())
                .totalElements(resultPage.getTotalElements())
                .first(resultPage.isFirst())
                .last(resultPage.isLast())
                .build();

        log.info("Retrieved {} results for batch: {} (total: {}, page {}/{})",
                rows.size(), batchId, resultPage.getTotalElements(), page + 1, resultPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to convert CalculationResult entity to BulkRowResult DTO
     * Note: This method doesn't set rowIndex as it's used in pagination contexts where row indexing is handled separately
     */
    private BulkRowResult convertToRowResult(CalculationResult r) {
        return BulkRowResult.builder()
                .rowIndex(0) // Will be set by the calling method based on context
                .employeeCode(r.getEmployeeCode())
                .employeeName(r.getEmployeeName() != null ? r.getEmployeeName() : "N/A")
                .jobTitle(r.getJobTitle())
                .yearsExperience(r.getYearsExperience())
                .performanceRating5(r.getPerfBucket() == 3 ? 5 : r.getPerfBucket() == 2 ? 3 : 2)
                .currentSalary(r.getCurrentSalary())
                .midOfScale(r.getMidOfScale())
                .compaRatio(r.getCompaRatio())
                .compaLabel(r.getCompaLabel())
                .increasePct(r.getIncreasePct())
                .newSalary(r.getNewSalary())
                .increaseAmount(r.getNewSalary().subtract(r.getCurrentSalary()))
                .build();
    }
}
