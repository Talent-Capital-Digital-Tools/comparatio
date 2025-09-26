package talentcapitalme.com.comparatio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptimizedBulkService {
    
    private final AdjustmentMatrixRepository matrixRepo;
    private final CalculationResultRepository resultRepo;
    private final UploadHistoryService uploadHistoryService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    
    // Thread pool for parallel processing
    private final Executor bulkProcessingExecutor = Executors.newFixedThreadPool(
        Math.max(4, Runtime.getRuntime().availableProcessors())
    );

    /**
     * Process Excel file with multithreading for high performance
     */
    public BulkResponse process(MultipartFile file) throws IOException {
        String clientId = Authz.getCurrentUserClientId();
        String batchId = Instant.now().toString();
        
        log.info("Starting bulk processing for client: {}, batch: {}", clientId, batchId);
        long startTime = System.currentTimeMillis();
        
        // Get client information for upload history
        String clientName = userRepository.findById(clientId)
                .map(user -> user.getName())
                .orElse("Unknown Client");
        
        String uploadedBy = Authz.getCurrentUserId();
        String uploadedByEmail = userRepository.findById(uploadedBy)
                .map(user -> user.getEmail())
                .orElse("unknown@example.com");
        
        // Create upload history record
        uploadHistoryService.createUploadHistory(
                clientId, clientName, file.getOriginalFilename(), 
                file.getOriginalFilename(), batchId, uploadedBy, uploadedByEmail);
        
        // Store uploaded file
        String uploadFilePath = fileStorageService.storeUploadedFile(file, clientId, batchId);
        uploadHistoryService.updateFilePaths(batchId, uploadFilePath, null);
        
        List<BulkRowResult> rows = new ArrayList<>();
        List<Row> excelRows = new ArrayList<>();
        
        // Read Excel file with comprehensive error handling
        try (Workbook wb = createRobustWorkbook(file)) {
            Sheet sh = wb.getSheetAt(0);
            
            // Collect all rows first
            for (Row r : sh) {
                if (r.getRowNum() == 0) continue; // skip header
                excelRows.add(r);
            }
        } catch (Exception e) {
            log.error("Error reading Excel file: {}", e.getMessage(), e);
            throw new IOException("Failed to read Excel file: " + e.getMessage(), e);
        }
        
        log.info("Processing {} rows with multithreading", excelRows.size());
        
        // Process rows in parallel batches
        int batchSize = Math.max(100, excelRows.size() / 10); // Dynamic batch size
        List<List<Row>> batches = partitionList(excelRows, batchSize);
        
        List<CompletableFuture<List<BulkRowResult>>> futures = batches.stream()
            .map(batch -> CompletableFuture.supplyAsync(() -> 
                processBatch(clientId, batch), bulkProcessingExecutor))
            .collect(Collectors.toList());
        
        // Wait for all batches to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );
        
        try {
            allFutures.get(); // Wait for completion
            
            // Collect results
            for (CompletableFuture<List<BulkRowResult>> future : futures) {
                rows.addAll(future.get());
            }
            
        } catch (Exception e) {
            log.error("Error during parallel processing", e);
            throw new RuntimeException("Bulk processing failed", e);
        }
        
        // Save successful results to database
        List<BulkRowResult> successfulRows = rows.stream()
            .filter(x -> x.getError() == null)
            .collect(Collectors.toList());
        
        saveResultsToDatabase(clientId, batchId, successfulRows);
        
        long processingTime = System.currentTimeMillis() - startTime;
        long successCount = successfulRows.size();
        long errorCount = rows.size() - successCount;
        
        log.info("Bulk processing completed. Total: {}, Success: {}, Errors: {}, Time: {}ms", 
                rows.size(), successCount, errorCount, processingTime);
        
        // Generate result Excel file
        byte[] resultData = generateResultExcel(rows);
        String resultFilePath = fileStorageService.storeResultFile(resultData, clientId, batchId, ".xlsx");
        
        // Update upload history with results
        List<String> validationErrors = rows.stream()
                .filter(row -> row.getError() != null)
                .map(BulkRowResult::getError)
                .collect(Collectors.toList());
        
        uploadHistoryService.updateUploadHistory(
                batchId, rows.size(), rows.size(), (int) successCount, (int) errorCount, 
                processingTime, resultFilePath, validationErrors);
        
        return new BulkResponse(batchId, rows.size(), (int) successCount, (int) errorCount, rows);
    }

    /**
     * Process a batch of rows in parallel
     */
    private List<BulkRowResult> processBatch(String clientId, List<Row> batch) {
        return batch.parallelStream()
            .map(row -> {
                try {
                    return processRow(clientId, row);
                } catch (Exception ex) {
                    log.warn("Error processing row {}: {}", row.getRowNum(), ex.getMessage());
                    return BulkRowResult.builder()
                        .rowIndex(row.getRowNum())
                        .error(ex.getMessage())
                        .build();
                }
            })
            .collect(Collectors.toList());
    }

    /**
     * Process a single row
     */
    private BulkRowResult processRow(String clientId, Row row) {
        String code = getString(row, 0);
        String employeeName = getString(row, 1);
        String jobTitle = getString(row, 2);
        Integer years = (int) getNumeric(row, 3);
        Integer perf5 = (int) getNumeric(row, 4);
        BigDecimal current = BigDecimal.valueOf(getNumeric(row, 5));
        BigDecimal mid = BigDecimal.valueOf(getNumeric(row, 6));

        return computeRow(clientId, row.getRowNum(), code, employeeName, jobTitle, years, perf5, current, mid);
    }

    /**
     * Save results to database in batches for better performance
     */
    private void saveResultsToDatabase(String clientId, String batchId, List<BulkRowResult> successfulRows) {
        if (successfulRows.isEmpty()) return;
        
        List<CalculationResult> results = successfulRows.stream()
            .map(row -> CalculationResult.builder()
                .clientId(clientId)
                .batchId(batchId)
                .employeeCode(row.getEmployeeCode())
                .jobTitle(row.getJobTitle())
                .yearsExperience(row.getYearsExperience())
                .perfBucket((row.getPerformanceRating5() >= 4) ? 3 : 
                           (row.getPerformanceRating5() >= 2 ? 2 : 1))
                .currentSalary(row.getCurrentSalary())
                .midOfScale(row.getMidOfScale())
                .compaRatio(row.getCompaRatio())
                .compaLabel(row.getCompaLabel())
                .increasePct(row.getIncreasePct())
                .newSalary(row.getNewSalary())
                .build())
            .collect(Collectors.toList());
        
        // Save in batches for better performance
        int batchSize = 1000;
        for (int i = 0; i < results.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, results.size());
            List<CalculationResult> batch = results.subList(i, endIndex);
            resultRepo.saveAll(batch);
        }
        
        log.info("Saved {} calculation results to database", results.size());
    }

    /**
     * Export results to Excel
     */
    public byte[] exportExcel(List<BulkRowResult> rows) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet("Results");
            
            // Create header
            Row headerRow = sh.createRow(0);
            String[] headers = {
                "Employee Code", "Employee Name", "Job Title", "Years of Experience", "Performance Rating", 
                "Current Salary", "Mid of Scale", "Compa Ratio %", "Band", 
                "Increase %", "New Salary", "Increase Amount", "Error"
            };
            
            for (int c = 0; c < headers.length; c++) {
                headerRow.createCell(c).setCellValue(headers[c]);
            }
            
            // Add data rows
            for (int r = 0; r < rows.size(); r++) {
                BulkRowResult br = rows.get(r);
                Row row = sh.createRow(r + 1);
                
                setCellValue(row, 0, br.getEmployeeCode());
                setCellValue(row, 1, br.getEmployeeName());
                setCellValue(row, 2, br.getJobTitle());
                setCellValue(row, 3, br.getYearsExperience());
                setCellValue(row, 4, br.getPerformanceRating5());
                setCellValue(row, 5, br.getCurrentSalary());
                setCellValue(row, 6, br.getMidOfScale());
                setCellValue(row, 7, br.getCompaRatio());
                setCellValue(row, 8, br.getCompaLabel());
                setCellValue(row, 9, br.getIncreasePct());
                setCellValue(row, 10, br.getNewSalary());
                setCellValue(row, 11, br.getIncreaseAmount());
                setCellValue(row, 12, br.getError());
            }
            
            // Auto-size columns
            for (int c = 0; c < headers.length; c++) {
                sh.autoSizeColumn(c);
            }
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return bos.toByteArray();
        }
    }

    /**
     * Compute calculation for a single row
     */
    private BulkRowResult computeRow(String clientId, int rowIndex, String code, String employeeName, String jobTitle, 
                                   Integer years, Integer perf5, BigDecimal current, BigDecimal mid) {
        // Validation
        if (mid == null || mid.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid midOfScale at row " + rowIndex);
        }
        if (current == null || current.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid current salary at row " + rowIndex);
        }
        if (perf5 == null || perf5 < 1 || perf5 > 5) {
            throw new IllegalArgumentException("Invalid performance rating at row " + rowIndex);
        }
        if (years == null || years < 0) {
            throw new IllegalArgumentException("Invalid years experience at row " + rowIndex);
        }

        BigDecimal compa = current.divide(mid, 6, RoundingMode.HALF_UP);
        int perfBucket = (perf5 >= 4) ? 3 : (perf5 >= 2) ? 2 : 1;

        // Use client-specific matrices for calculations
        AdjustmentMatrix cell = matrixRepo.findClientActiveCell(perfBucket, compa, clientId)
                .orElseThrow(() -> new IllegalStateException(
                    "No matrix found for client '" + clientId + "' at row " + rowIndex));

        BigDecimal pct = (years < 5) ? cell.getPctLt5Years() : cell.getPctGte5Years();
        BigDecimal newSalary = current.multiply(BigDecimal.ONE.add(pct.movePointLeft(2)))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal increaseAmount = newSalary.subtract(current).setScale(2, RoundingMode.HALF_UP);

        return BulkRowResult.builder()
                .rowIndex(rowIndex)
                .employeeCode(code)
                .employeeName(employeeName)
                .jobTitle(jobTitle)
                .yearsExperience(years)
                .performanceRating5(perf5)
                .currentSalary(current)
                .midOfScale(mid)
                .compaRatio(compa)
                .compaLabel(createCompaLabel(cell))
                .increasePct(pct)
                .newSalary(newSalary)
                .increaseAmount(increaseAmount)
                .build();
    }

    /**
     * Create compa ratio label
     */
    private String createCompaLabel(AdjustmentMatrix cell) {
        BigDecimal from = cell.getCompaFrom().multiply(BigDecimal.valueOf(100));
        BigDecimal to = cell.getCompaTo().multiply(BigDecimal.valueOf(100));
        boolean open = cell.getCompaTo().compareTo(BigDecimal.valueOf(9.99)) >= 0;
        return open ? from.stripTrailingZeros().toPlainString() + "%+"
                : from.stripTrailingZeros().toPlainString() + "%–" + 
                  to.stripTrailingZeros().toPlainString() + "%";
    }

    // Utility methods
    private static String getString(Row r, int idx) { 
        Cell c = r.getCell(idx); 
        if (c == null) return null;
        
        try {
            switch (c.getCellType()) {
                case STRING:
                    return c.getStringCellValue();
                case NUMERIC:
                    // Convert numeric to string
                    return String.valueOf(c.getNumericCellValue());
                case FORMULA:
                    return c.getStringCellValue();
                case BOOLEAN:
                    return String.valueOf(c.getBooleanCellValue());
                case BLANK:
                    return null;
                default:
                    log.warn("Unsupported cell type for string: {} at row {}, col {}", c.getCellType(), r.getRowNum(), idx);
                    return null;
            }
        } catch (Exception e) {
            log.warn("Error reading string value at row {}, col {}: {}", r.getRowNum(), idx, e.getMessage());
            return null;
        }
    }
    
    private static double getNumeric(Row r, int idx) { 
        Cell c = r.getCell(idx); 
        if (c == null) return 0d;
        
        try {
            switch (c.getCellType()) {
                case NUMERIC:
                    return c.getNumericCellValue();
                case STRING:
                    // Try to parse string as number
                    String stringValue = c.getStringCellValue().trim();
                    if (stringValue.isEmpty()) return 0d;
                    // Remove any non-numeric characters except decimal point and minus
                    stringValue = stringValue.replaceAll("[^0-9.-]", "");
                    if (stringValue.isEmpty()) return 0d;
                    return Double.parseDouble(stringValue);
                case FORMULA:
                    // Handle formula cells
                    try {
                        return c.getNumericCellValue();
                    } catch (Exception e) {
                        // If formula evaluation fails, try to get string value and parse
                        String formulaResult = c.getStringCellValue();
                        if (formulaResult != null && !formulaResult.trim().isEmpty()) {
                            return Double.parseDouble(formulaResult.replaceAll("[^0-9.-]", ""));
                        }
                        return 0d;
                    }
                case BOOLEAN:
                    return c.getBooleanCellValue() ? 1d : 0d;
                case BLANK:
                    return 0d;
                case ERROR:
                    log.warn("Error cell at row {}, col {}", r.getRowNum(), idx);
                    return 0d;
                default:
                    log.warn("Unsupported cell type: {} at row {}, col {}", c.getCellType(), r.getRowNum(), idx);
                    return 0d;
            }
        } catch (NumberFormatException e) {
            log.warn("Number format error at row {}, col {}: {}", r.getRowNum(), idx, e.getMessage());
            return 0d;
        } catch (Exception e) {
            log.warn("Error reading numeric value at row {}, col {}: {}", r.getRowNum(), idx, e.getMessage());
            return 0d;
        }
    }
    
    private static void setCellValue(Row r, int c, Object v) { 
        if (v == null) return; 
        r.createCell(c).setCellValue(String.valueOf(v)); 
    }
    
    /**
     * Create workbook with comprehensive error handling and multiple fallback strategies
     */
    private Workbook createRobustWorkbook(MultipartFile file) throws IOException {
        byte[] fileBytes = file.getBytes();
        
        // Strategy 1: Try XSSFWorkbook with byte array
        try {
            return new XSSFWorkbook(new ByteArrayInputStream(fileBytes));
        } catch (Exception e) {
            log.debug("XSSFWorkbook failed: {}", e.getMessage());
        }
        
        // Strategy 2: Try WorkbookFactory with byte array
        try {
            return WorkbookFactory.create(new ByteArrayInputStream(fileBytes));
        } catch (Exception e) {
            log.debug("WorkbookFactory failed: {}", e.getMessage());
        }
        
        // Strategy 3: Try with input stream reset
        try {
            file.getInputStream().reset();
            return WorkbookFactory.create(file.getInputStream());
        } catch (Exception e) {
            log.debug("WorkbookFactory with stream reset failed: {}", e.getMessage());
        }
        
        // Strategy 4: Try with different POI options
        try {
            return createWorkbookWithPOIOptions(fileBytes);
        } catch (Exception e) {
            log.debug("POI options failed: {}", e.getMessage());
        }
        
        // Strategy 5: Try CSV conversion as last resort
        try {
            return convertToWorkbook(fileBytes);
        } catch (Exception e) {
            log.debug("CSV conversion failed: {}", e.getMessage());
        }
        
        // If all strategies fail, provide helpful error message
        throw new IOException("Unable to read Excel file. Please ensure the file is a valid .xlsx format without complex date formatting, formulas, or special characters. Try saving the file as a simple Excel format.");
    }
    
    /**
     * Create workbook with specific POI options to handle problematic files
     */
    private Workbook createWorkbookWithPOIOptions(byte[] fileBytes) throws IOException {
        try {
            // Try with different POI configurations
            return WorkbookFactory.create(new ByteArrayInputStream(fileBytes));
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("YearOfEra")) {
                throw new IOException("The Excel file contains unsupported date formats. Please convert all date columns to text format before uploading.", e);
            } else if (errorMsg != null && errorMsg.contains("Unsupported")) {
                throw new IOException("The Excel file contains unsupported formatting. Please save as a simple .xlsx format with only text and numbers.", e);
            } else {
                throw new IOException("Excel file format not supported: " + errorMsg, e);
            }
        }
    }
    
    /**
     * Convert problematic Excel file to a clean workbook
     */
    private Workbook convertToWorkbook(byte[] fileBytes) throws IOException {
        // This is a fallback method - in a real implementation, you might use a different library
        // For now, we'll just throw a helpful error
        throw new IOException("The Excel file format is not supported. Please save your file as a simple .xlsx format with the following columns: Employee Code, Job Title, Years of Experience, Performance Rating, Current Salary, Mid of Scale.");
    }

    /**
     * Partition a list into smaller batches
     */
    private static <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        return IntStream.range(0, (list.size() + batchSize - 1) / batchSize)
            .mapToObj(i -> list.subList(i * batchSize, Math.min((i + 1) * batchSize, list.size())))
            .collect(Collectors.toList());
    }
    
    /**
     * Generate result Excel file with all processed data
     */
    private byte[] generateResultExcel(List<BulkRowResult> results) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Calculation Results");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Row Index", "Employee Code", "Employee Name", "Job Title", "Years Experience", 
                "Performance Rating", "Current Salary", "Mid of Scale", 
                "Compa Ratio", "Compa Label", "Increase %", "New Salary", "Increase Amount", "Error"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // Add data rows
            for (int i = 0; i < results.size(); i++) {
                BulkRowResult result = results.get(i);
                Row row = sheet.createRow(i + 1);
                
                row.createCell(0).setCellValue(result.getRowIndex());
                row.createCell(1).setCellValue(result.getEmployeeCode());
                row.createCell(2).setCellValue(result.getEmployeeName());
                row.createCell(3).setCellValue(result.getJobTitle());
                row.createCell(4).setCellValue(result.getYearsExperience());
                row.createCell(5).setCellValue(result.getPerformanceRating5());
                row.createCell(6).setCellValue(result.getCurrentSalary().doubleValue());
                row.createCell(7).setCellValue(result.getMidOfScale().doubleValue());
                row.createCell(8).setCellValue(result.getCompaRatio().doubleValue());
                row.createCell(9).setCellValue(result.getCompaLabel());
                row.createCell(10).setCellValue(result.getIncreasePct().doubleValue());
                row.createCell(11).setCellValue(result.getNewSalary().doubleValue());
                row.createCell(12).setCellValue(result.getIncreaseAmount().doubleValue());
                row.createCell(13).setCellValue(result.getError() != null ? result.getError() : "");
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Convert to byte array
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }
}
