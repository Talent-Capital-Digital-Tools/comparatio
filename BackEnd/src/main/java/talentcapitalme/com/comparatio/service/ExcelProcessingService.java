package talentcapitalme.com.comparatio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import talentcapitalme.com.comparatio.dto.BulkResponse;
import talentcapitalme.com.comparatio.dto.BulkRowResult;
import talentcapitalme.com.comparatio.entity.AdjustmentMatrix;
import talentcapitalme.com.comparatio.entity.CalculationResult;
import talentcapitalme.com.comparatio.repository.AdjustmentMatrixRepository;
import talentcapitalme.com.comparatio.repository.CalculationResultRepository;
import talentcapitalme.com.comparatio.repository.UserRepository;
import talentcapitalme.com.comparatio.security.Authz;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Professional Excel processing service for compensation ratio calculations
 * Uses Apache POI for robust Excel file processing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelProcessingService implements IExcelProcessingService {

    private final AdjustmentMatrixRepository matrixRepo;
    private final CalculationResultRepository resultRepo;
    private final UploadHistoryService uploadHistoryService;
    private final UserRepository userRepository;

    /**
     * Process Excel file with comprehensive validation and error handling
     */
    public BulkResponse processExcelFile(MultipartFile file) throws IOException {
        String clientId = Authz.getCurrentUserClientId();
        String batchId = Instant.now().toString();
        
        log.info("Starting Excel processing for client: {}, batch: {}, file: {}", 
                clientId, batchId, file.getOriginalFilename());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate file
            validateExcelFile(file);
            
            // Create upload history
            createUploadHistory(clientId, file, batchId);
            
            // Process Excel file
            List<BulkRowResult> results = processExcelData(file, clientId, batchId);
            
            // Save results to database
            saveCalculationResults(results, clientId, batchId);
            
            // Generate response
            BulkResponse response = buildBulkResponse(results, batchId);
            
            long processingTime = System.currentTimeMillis() - startTime;
            log.info("Excel processing completed in {}ms - Total: {}, Success: {}, Errors: {}", 
                    processingTime, response.getTotalRows(), response.getSuccessCount(), response.getErrorCount());
            
            return response;
            
        } catch (Exception e) {
            log.error("Error processing Excel file: {}", e.getMessage(), e);
            throw new IOException("Failed to process Excel file: " + e.getMessage(), e);
        }
    }

    /**
     * Generate enhanced Excel file with calculation results
     */
    public byte[] generateEnhancedExcel(List<BulkRowResult> results, String batchId) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Compensation Results");
            
            // Create header row with styling
            createHeaderRow(workbook, sheet);
            
            // Add data rows
            int rowIndex = 1;
            for (BulkRowResult result : results) {
                createDataRow(workbook, sheet, result, rowIndex++);
            }
            
            // Auto-size columns
            autoSizeColumns(sheet);
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Validate Excel file before processing
     */
    private void validateExcelFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Excel file is required");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new IllegalArgumentException("Only Excel files (.xlsx, .xls) are supported");
        }
        
        if (file.getSize() > 50 * 1024 * 1024) { // 50MB limit
            throw new IllegalArgumentException("File size exceeds 50MB limit");
        }
    }

    /**
     * Create upload history record
     */
    private void createUploadHistory(String clientId, MultipartFile file, String batchId) {
        try {
            String clientName = userRepository.findById(clientId)
                    .map(user -> user.getName())
                    .orElse("Unknown Client");
            
            String uploadedBy = Authz.getCurrentUserId();
            String uploadedByEmail = userRepository.findById(uploadedBy)
                    .map(user -> user.getEmail())
                    .orElse("unknown@example.com");
            
            uploadHistoryService.createUploadHistory(
                    clientId, clientName, file.getOriginalFilename(), 
                    file.getOriginalFilename(), batchId, uploadedBy, uploadedByEmail);
        } catch (Exception e) {
            log.warn("Failed to create upload history: {}", e.getMessage());
        }
    }

    /**
     * Process Excel data using Apache POI
     */
    private List<BulkRowResult> processExcelData(MultipartFile file, String clientId, String batchId) throws IOException {
        List<BulkRowResult> results = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("Excel file must contain at least one sheet");
            }
            
            // Validate header row
            validateHeaderRow(sheet);
            
            // Process data rows
            int lastRowNum = sheet.getLastRowNum();
            int processedRows = 0;
            int skippedRows = 0;
            int errorRows = 0;
            
            log.info("Processing Excel file with {} total rows (including header)", lastRowNum + 1);
            
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                
                // Skip completely empty rows
                if (row == null || isRowEmpty(row)) {
                    log.debug("Skipping empty row {}", i);
                    skippedRows++;
                    continue;
                }
                
                try {
                    BulkRowResult result = processRow(row, clientId, i); // Use actual Excel row number
                    results.add(result);
                    processedRows++;
                } catch (Exception e) {
                    log.warn("Error processing Excel row {}: {}", i, e.getMessage());
                    results.add(createErrorResultWithOriginalData(row, i, e.getMessage()));
                    errorRows++;
                }
            }
            
            log.info("Excel processing summary - Processed: {}, Skipped (empty): {}, Errors: {}, Total results: {}", 
                    processedRows, skippedRows, errorRows, results.size());
        }
        
        return results;
    }

    /**
     * Check if a row is completely empty
     */
    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        
        // Check if the row has any cells at all
        if (row.getLastCellNum() == -1) return true;
        
        // Check first 3 columns (Employee Code, Name, Job Title) - if all are empty, consider row empty
        boolean hasAnyData = false;
        for (int i = 0; i < 3; i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                String cellValue = getCellValueAsString(cell);
                if (cellValue != null && !cellValue.trim().isEmpty()) {
                    hasAnyData = true;
                    break;
                }
            }
        }
        
        return !hasAnyData;
    }

    /**
     * Process a single row
     */
    private BulkRowResult processRow(Row row, String clientId, int rowIndex) {
        // Extract data from row
        String employeeCode = getCellValueAsString(row.getCell(0));
        String employeeName = getCellValueAsString(row.getCell(1));
        String jobTitle = getCellValueAsString(row.getCell(2));
        Integer yearsExperience = getCellValueAsInteger(row.getCell(3));
        Integer performanceRating = getCellValueAsInteger(row.getCell(4));
        BigDecimal currentSalary = getCellValueAsBigDecimal(row.getCell(5));
        BigDecimal midOfScale = getCellValueAsBigDecimal(row.getCell(6));
        
        // Log the extracted data for debugging
        log.info("Processing Excel row {}: EmployeeCode={}, EmployeeName={}, JobTitle={}, YearsExp={}, PerfRating={}, CurrentSalary={}, MidOfScale={}", 
                rowIndex, employeeCode, employeeName, jobTitle, yearsExperience, performanceRating, currentSalary, midOfScale);
        
        // Validate required fields
        validateRowData(employeeCode, employeeName, jobTitle, yearsExperience, performanceRating, currentSalary, midOfScale, rowIndex);
        
        // Perform calculation
        return calculateCompensation(clientId, rowIndex, employeeCode, employeeName, jobTitle, 
                yearsExperience, performanceRating, currentSalary, midOfScale);
    }

    /**
     * Validate header row structure
     */
    private void validateHeaderRow(Sheet sheet) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("Excel file must have a header row");
        }
        
        String[] expectedHeaders = {"Employee Code", "Employee Name", "Job Title", "Years of Experience", 
                                  "Performance Rating", "Current Salary", "Mid of Scale"};
        
        for (int i = 0; i < expectedHeaders.length; i++) {
            String cellValue = getCellValueAsString(headerRow.getCell(i));
            if (cellValue == null || !isValidHeader(cellValue.trim(), expectedHeaders[i])) {
                throw new IllegalArgumentException(
                    String.format("Invalid header at column %d. Expected '%s', found '%s'", 
                                i + 1, expectedHeaders[i], cellValue));
            }
        }
    }

    /**
     * Check if header value matches expected header (case-insensitive and flexible)
     */
    private boolean isValidHeader(String actualHeader, String expectedHeader) {
        if (actualHeader == null || expectedHeader == null) {
            return false;
        }
        
        // Normalize both strings for comparison
        String normalizedActual = actualHeader.toLowerCase().replaceAll("\\s+", " ").trim();
        String normalizedExpected = expectedHeader.toLowerCase().replaceAll("\\s+", " ").trim();
        
        // Direct match
        if (normalizedActual.equals(normalizedExpected)) {
            return true;
        }
        
        // Handle common variations
        switch (normalizedExpected) {
            case "years of experience":
                return normalizedActual.equals("years of experience") || 
                       normalizedActual.equals("years experience") ||
                       normalizedActual.equals("experience years");
            case "performance rating":
                return normalizedActual.equals("performance rating") ||
                       normalizedActual.equals("rating") ||
                       normalizedActual.equals("perf rating") ||
                       normalizedActual.contains("performance rating") ||
                       normalizedActual.startsWith("performance rating");
            case "current salary":
                return normalizedActual.equals("current salary") ||
                       normalizedActual.equals("salary") ||
                       normalizedActual.equals("current pay");
            case "mid of scale":
                return normalizedActual.equals("mid of scale") ||
                       normalizedActual.equals("mid scale") ||
                       normalizedActual.equals("midpoint") ||
                       normalizedActual.equals("mid point");
            case "employee code":
                return normalizedActual.equals("employee code") ||
                       normalizedActual.equals("emp code") ||
                       normalizedActual.equals("employee id");
            case "employee name":
                return normalizedActual.equals("employee name") ||
                       normalizedActual.equals("name") ||
                       normalizedActual.equals("emp name");
            case "job title":
                return normalizedActual.equals("job title") ||
                       normalizedActual.equals("title") ||
                       normalizedActual.equals("position");
            default:
                return false;
        }
    }

    /**
     * Validate row data
     */
    private void validateRowData(String employeeCode, String employeeName, String jobTitle, Integer yearsExperience,
                               Integer performanceRating, BigDecimal currentSalary, 
                               BigDecimal midOfScale, int rowIndex) {
        if (employeeCode == null || employeeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee Code is required at row " + rowIndex);
        }
        if (employeeName == null || employeeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee Name is required at row " + rowIndex);
        }
        if (jobTitle == null || jobTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Job Title is required at row " + rowIndex);
        }
        if (yearsExperience == null) {
            throw new IllegalArgumentException("Years Experience is required at row " + rowIndex);
        }
        if (yearsExperience < 0) {
            throw new IllegalArgumentException("Years Experience must be non-negative at row " + rowIndex);
        }
        if (performanceRating == null) {
            throw new IllegalArgumentException("Performance Rating is required at row " + rowIndex);
        }
        if (performanceRating < 1 || performanceRating > 5) {
            throw new IllegalArgumentException("Performance Rating must be between 1 and 5 at row " + rowIndex);
        }
        if (currentSalary == null) {
            throw new IllegalArgumentException("Current Salary is required at row " + rowIndex);
        }
        if (currentSalary.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Current Salary must be positive at row " + rowIndex);
        }
        if (midOfScale == null) {
            throw new IllegalArgumentException("Mid of Scale is required at row " + rowIndex);
        }
        if (midOfScale.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Mid of Scale must be positive at row " + rowIndex);
        }
    }

    /**
     * Calculate compensation using business logic
     */
    private BulkRowResult calculateCompensation(String clientId, int rowIndex, String employeeCode, 
                                              String employeeName, String jobTitle, Integer yearsExperience, 
                                              Integer performanceRating, BigDecimal currentSalary, 
                                              BigDecimal midOfScale) {
        
        // Calculate compa ratio as percentage (integer)
        BigDecimal compaRatioDecimal = currentSalary.divide(midOfScale, 4, RoundingMode.HALF_UP);
        BigDecimal compaRatio = compaRatioDecimal.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP);
        
        // Determine performance bucket
        int perfBucket = (performanceRating >= 4) ? 3 : (performanceRating >= 2) ? 2 : 1;
        
        // Find appropriate adjustment matrix (convert percentage back to decimal for lookup)
        BigDecimal compaRatioForLookup = compaRatio.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        Optional<AdjustmentMatrix> matrixOpt = matrixRepo.findClientActiveCell(perfBucket, compaRatioForLookup, clientId);
        
        if (matrixOpt.isEmpty()) {
            // No matrix found - use default calculation or zero increase
            log.warn("No adjustment matrix found for client '{}' with performance bucket {} and compa ratio {} at row {}. Using zero increase.", 
                    clientId, perfBucket, compaRatio, rowIndex);
            
            return BulkRowResult.builder()
                    .rowIndex(rowIndex)
                    .employeeCode(employeeCode)
                    .employeeName(employeeName)
                    .jobTitle(jobTitle)
                    .yearsExperience(yearsExperience)
                    .performanceRating5(performanceRating)
                    .currentSalary(currentSalary)
                    .midOfScale(midOfScale)
                    .compaRatio(compaRatio)
                    .compaLabel(determineCompaLabel(compaRatio))
                    .increasePct(BigDecimal.ZERO)
                    .newSalary(currentSalary)
                    .increaseAmount(BigDecimal.ZERO)
                    .build();
        }
        
        AdjustmentMatrix matrix = matrixOpt.get();
        
        // Calculate percentage increase based on experience
        BigDecimal increasePct = (yearsExperience < 5) ? matrix.getPctLt5Years() : matrix.getPctGte5Years();
        
        // Calculate new salary
        BigDecimal newSalary = currentSalary.multiply(BigDecimal.ONE.add(increasePct.movePointLeft(2)))
                .setScale(2, RoundingMode.HALF_UP);
        
        // Calculate increase amount
        BigDecimal increaseAmount = newSalary.subtract(currentSalary).setScale(2, RoundingMode.HALF_UP);
        
        // Determine compa label
        String compaLabel = determineCompaLabel(compaRatio);
        
        return BulkRowResult.builder()
                .rowIndex(rowIndex)
                .employeeCode(employeeCode)
                .employeeName(employeeName)
                .jobTitle(jobTitle)
                .yearsExperience(yearsExperience)
                .performanceRating5(performanceRating)
                .currentSalary(currentSalary)
                .midOfScale(midOfScale)
                .compaRatio(compaRatio)
                .compaLabel(compaLabel)
                .increasePct(increasePct)
                .newSalary(newSalary)
                .increaseAmount(increaseAmount)
                .build();
    }

    /**
     * Determine compa label based on ratio
     */
    private String determineCompaLabel(BigDecimal compaRatio) {
        if (compaRatio.compareTo(BigDecimal.valueOf(0.71)) < 0) {
            return "< 71%";
        } else if (compaRatio.compareTo(BigDecimal.valueOf(0.85)) < 0) {
            return "71% - 85%";
        } else if (compaRatio.compareTo(BigDecimal.valueOf(1.0)) < 0) {
            return "85% - 100%";
        } else if (compaRatio.compareTo(BigDecimal.valueOf(1.15)) < 0) {
            return "100% - 115%";
        } else {
            return "> 115%";
        }
    }

    /**
     * Create error result with original data preserved
     */
    private BulkRowResult createErrorResultWithOriginalData(Row row, int rowIndex, String errorMessage) {
        // Extract original data even if calculation failed
        String employeeCode = getCellValueAsString(row.getCell(0));
        String employeeName = getCellValueAsString(row.getCell(1));
        String jobTitle = getCellValueAsString(row.getCell(2));
        Integer yearsExperience = getCellValueAsInteger(row.getCell(3));
        Integer performanceRating = getCellValueAsInteger(row.getCell(4));
        BigDecimal currentSalary = getCellValueAsBigDecimal(row.getCell(5));
        BigDecimal midOfScale = getCellValueAsBigDecimal(row.getCell(6));
        
        log.info("Preserving original data for failed row {}: EmployeeCode={}, EmployeeName={}, JobTitle={}, YearsExp={}, PerfRating={}, CurrentSalary={}, MidOfScale={}", 
                rowIndex, employeeCode, employeeName, jobTitle, yearsExperience, performanceRating, currentSalary, midOfScale);
        
        return BulkRowResult.builder()
                .rowIndex(rowIndex)
                .employeeCode(employeeCode)
                .employeeName(employeeName)
                .jobTitle(jobTitle)
                .yearsExperience(yearsExperience)
                .performanceRating5(performanceRating)
                .currentSalary(currentSalary)
                .midOfScale(midOfScale)
                .error(errorMessage)
                .build();
    }

    /**
     * Save calculation results to database
     */
    private void saveCalculationResults(List<BulkRowResult> results, String clientId, String batchId) {
        List<CalculationResult> calculationResults = results.stream()
                .filter(result -> result.getError() == null)
                .map(result -> CalculationResult.builder()
                        .clientId(clientId)
                        .batchId(batchId)
                        .employeeCode(result.getEmployeeCode())
                        .employeeName(result.getEmployeeName())  // Now saving employee name
                        .jobTitle(result.getJobTitle())
                        .yearsExperience(result.getYearsExperience())
                        .perfBucket((result.getPerformanceRating5() >= 4) ? 3 : 
                                   (result.getPerformanceRating5() >= 2) ? 2 : 1)
                        .currentSalary(result.getCurrentSalary())
                        .midOfScale(result.getMidOfScale())
                        .compaRatio(result.getCompaRatio())
                        .compaLabel(result.getCompaLabel())
                        .increasePct(result.getIncreasePct())
                        .newSalary(result.getNewSalary())
                        .build())
                .collect(Collectors.toList());
        
        if (!calculationResults.isEmpty()) {
            resultRepo.saveAll(calculationResults);
            log.info("Saved {} calculation results to database", calculationResults.size());
        }
    }

    /**
     * Build bulk response
     */
    private BulkResponse buildBulkResponse(List<BulkRowResult> results, String batchId) {
        int successCount = (int) results.stream().filter(r -> r.getError() == null).count();
        int errorCount = (int) results.stream().filter(r -> r.getError() != null).count();
        
        return new BulkResponse(batchId, results.size(), successCount, errorCount, results);
    }

    /**
     * Create header row with styling
     */
    private void createHeaderRow(Workbook workbook, Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        
        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        
        // Set header values
        String[] headers = {"Employee Code", "Employee Name", "Job Title", "Years of Experience", "Performance Rating",
                           "Current Salary", "Mid of Scale", "Compa Ratio",
                           "Increase %", "New Salary", "Increase Amount"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * Create data row
     */
    private void createDataRow(Workbook workbook, Sheet sheet, BulkRowResult result, int rowIndex) {
        Row row = sheet.createRow(rowIndex);
        
        // Create data style
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setWrapText(true);
        
        // Set cell values
        int colIndex = 0;
        row.createCell(colIndex++).setCellValue(result.getEmployeeCode() != null ? result.getEmployeeCode() : "");
        row.createCell(colIndex++).setCellValue(result.getEmployeeName() != null ? result.getEmployeeName() : "");
        row.createCell(colIndex++).setCellValue(result.getJobTitle() != null ? result.getJobTitle() : "");
        row.createCell(colIndex++).setCellValue(result.getYearsExperience() != null ? result.getYearsExperience() : 0);
        row.createCell(colIndex++).setCellValue(result.getPerformanceRating5() != null ? result.getPerformanceRating5() : 0);
        row.createCell(colIndex++).setCellValue(result.getCurrentSalary() != null ? result.getCurrentSalary().doubleValue() : 0.0);
        row.createCell(colIndex++).setCellValue(result.getMidOfScale() != null ? result.getMidOfScale().doubleValue() : 0.0);
        
        // For error rows, show "ERROR" in calculation columns, otherwise show calculated values
        if (result.getError() != null) {
            row.createCell(colIndex++).setCellValue("ERROR");
            row.createCell(colIndex++).setCellValue("ERROR");
            row.createCell(colIndex++).setCellValue("ERROR");
            row.createCell(colIndex++).setCellValue("ERROR");
        } else {
            row.createCell(colIndex++).setCellValue(result.getCompaRatio() != null ? result.getCompaRatio().doubleValue() : 0.0);
            row.createCell(colIndex++).setCellValue(result.getIncreasePct() != null ? result.getIncreasePct().doubleValue() : 0.0);
            row.createCell(colIndex++).setCellValue(result.getNewSalary() != null ? result.getNewSalary().doubleValue() : 0.0);
            row.createCell(colIndex++).setCellValue(result.getIncreaseAmount() != null ? result.getIncreaseAmount().doubleValue() : 0.0);
        }
        
        // Apply styling to all cells
        for (int i = 0; i < colIndex; i++) {
            row.getCell(i).setCellStyle(dataStyle);
        }
    }

    /**
     * Auto-size columns
     */
    private void autoSizeColumns(Sheet sheet) {
        Row headerRow = sheet.getRow(0);
        if (headerRow != null) {
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                sheet.autoSizeColumn(i);
            }
        }
    }

    // Helper methods for cell value extraction
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // Check if it's a whole number (likely an ID/code)
                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.floor(numericValue) && !Double.isInfinite(numericValue)) {
                    return String.valueOf((long) numericValue);
                } else {
                    return String.valueOf(numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }

    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case NUMERIC:
                return BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING:
                try {
                    return new BigDecimal(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }
}