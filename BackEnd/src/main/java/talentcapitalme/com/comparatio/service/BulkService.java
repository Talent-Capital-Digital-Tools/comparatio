package talentcapitalme.com.comparatio.service;

import lombok.RequiredArgsConstructor;
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
import talentcapitalme.com.comparatio.security.Authz;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BulkService {
    private final AdjustmentMatrixRepository matrixRepo;
    private final CalculationResultRepository resultRepo;

    public BulkResponse process(MultipartFile file) throws IOException {
        String clientId = Authz.getCurrentUserClientId();
        String batchId = Instant.now().toString();
        List<BulkRowResult> rows = new ArrayList<>();

        try (Workbook wb = createRobustWorkbook(file)) {
            Sheet sh = wb.getSheetAt(0);
            int i = 0;
            for (Row r : sh) {
                i++;
                if (i == 1) continue; // skip header
                try {
                    String code = getString(r, 0);
                    String jobTitle = getString(r, 1);
                    Integer years = (int) getNumeric(r, 2);
                    Integer perf5 = (int) getNumeric(r, 3);
                    BigDecimal current = BigDecimal.valueOf(getNumeric(r, 4));
                    BigDecimal mid = BigDecimal.valueOf(getNumeric(r, 5));

                    BulkRowResult one = computeRow(clientId, i, code, jobTitle, years, perf5, current, mid);
                    rows.add(one);
                } catch (Exception ex) {
                    rows.add(BulkRowResult.builder().rowIndex(i).error(ex.getMessage()).build());
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading Excel file: " + e.getMessage());
            throw new IOException("Failed to read Excel file: " + e.getMessage(), e);
        }

        long ok = rows.stream().filter(x -> x.getError() == null).count();

        rows.stream().filter(x -> x.getError() == null).forEach(x -> resultRepo.save(CalculationResult.builder()
                .clientId(clientId)
                .batchId(batchId)
                .employeeCode(x.getEmployeeCode())
                .jobTitle(x.getJobTitle())
                .yearsExperience(x.getYearsExperience())
                .perfBucket((x.getPerformanceRating5() >= 4) ? 3 : (x.getPerformanceRating5() >= 2 ? 2 : 1))
                .currentSalary(x.getCurrentSalary())
                .midOfScale(x.getMidOfScale())
                .compaRatio(x.getCompaRatio())
                .compaLabel(x.getCompaLabel())
                .increasePct(x.getIncreasePct())
                .newSalary(x.getNewSalary())
            
                .build()));

        return new BulkResponse(batchId, rows.size(), (int) ok, rows.size() - (int) ok, rows);
    }

    public byte[] exportExcel(List<BulkRowResult> rows) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet("Results");
            Row h = sh.createRow(0);
            String[] heads = {"EmployeeCode","JobTitle","YearsExperience","PerfRating5","CurrentSalary","MidOfScale","CompaRatio","Band","Increase%","NewSalary","Error"};
            for (int c = 0; c < heads.length; c++) h.createCell(c).setCellValue(heads[c]);
            int r = 1;
            for (BulkRowResult br : rows) {
                Row row = sh.createRow(r++);
                set(row,0, br.getEmployeeCode());
                set(row,1, br.getJobTitle());
                set(row,2, br.getYearsExperience());
                set(row,3, br.getPerformanceRating5());
                set(row,4, br.getCurrentSalary());
                set(row,5, br.getMidOfScale());
                set(row,6, br.getCompaRatio());
                set(row,7, br.getCompaLabel());
                set(row,8, br.getIncreasePct());
                set(row,9, br.getNewSalary());
                set(row,10, br.getError());
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return bos.toByteArray();
        }
    }

    private BulkRowResult computeRow(String clientId, int rowIndex, String code, String jobTitle, Integer years, Integer perf5, BigDecimal current, BigDecimal mid) {
        if (mid == null || mid.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Invalid midOfScale");
        LocalDate asOf = LocalDate.now();

        BigDecimal compa = current.divide(mid, 6, RoundingMode.HALF_UP);
        int perfBucket = (perf5 >= 4) ? 3 : (perf5 >= 2) ? 2 : 1;

        // Use client-specific matrices for calculations
        AdjustmentMatrix cell = matrixRepo.findClientActiveCell(perfBucket, compa, asOf, clientId)
                .orElseThrow(() -> new IllegalStateException("No matrix found for client '" + clientId + "' at row " + rowIndex));

        BigDecimal pct = (years < 5) ? cell.getPctLt5Years() : cell.getPctGte5Years();
        BigDecimal newSalary = current.multiply(BigDecimal.ONE.add(pct.movePointLeft(2)))
                .setScale(2, RoundingMode.HALF_UP);

        return BulkRowResult.builder()
                .rowIndex(rowIndex)
                .employeeCode(code)
                .jobTitle(jobTitle)
                .yearsExperience(years)
                .performanceRating5(perf5)
                .currentSalary(current)
                .midOfScale(mid)
                .compaRatio(compa)
                .compaLabel(label(cell))
                .increasePct(pct)
                .newSalary(newSalary)
                .build();
    }

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
                    System.out.println("Unsupported cell type for string: " + c.getCellType() + " at row " + r.getRowNum() + ", col " + idx);
                    return null;
            }
        } catch (Exception e) {
            System.out.println("Error reading string value at row " + r.getRowNum() + ", col " + idx + ": " + e.getMessage());
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
                    System.out.println("Error cell at row " + r.getRowNum() + ", col " + idx);
                    return 0d;
                default:
                    System.out.println("Unsupported cell type: " + c.getCellType() + " at row " + r.getRowNum() + ", col " + idx);
                    return 0d;
            }
        } catch (NumberFormatException e) {
            System.out.println("Number format error at row " + r.getRowNum() + ", col " + idx + ": " + e.getMessage());
            return 0d;
        } catch (Exception e) {
            System.out.println("Error reading numeric value at row " + r.getRowNum() + ", col " + idx + ": " + e.getMessage());
            return 0d;
        }
    }
    
    private static void set(Row r, int c, Object v) { 
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
            System.out.println("XSSFWorkbook failed: " + e.getMessage());
        }
        
        // Strategy 2: Try WorkbookFactory with byte array
        try {
            return WorkbookFactory.create(new ByteArrayInputStream(fileBytes));
        } catch (Exception e) {
            System.out.println("WorkbookFactory failed: " + e.getMessage());
        }
        
        // Strategy 3: Try with input stream reset
        try {
            file.getInputStream().reset();
            return WorkbookFactory.create(file.getInputStream());
        } catch (Exception e) {
            System.out.println("WorkbookFactory with stream reset failed: " + e.getMessage());
        }
        
        // If all strategies fail, provide helpful error message
        throw new IOException("Unable to read Excel file. Please ensure the file is a valid .xlsx format without complex date formatting, formulas, or special characters. Try saving the file as a simple Excel format.");
    }

    private String label(AdjustmentMatrix c) {
        BigDecimal from = c.getCompaFrom().multiply(BigDecimal.valueOf(100));
        BigDecimal to = c.getCompaTo().multiply(BigDecimal.valueOf(100));
        boolean open = c.getCompaTo().compareTo(BigDecimal.valueOf(9.99)) >= 0;
        return open ? from.stripTrailingZeros().toPlainString() + "%+"
                : from.stripTrailingZeros().toPlainString() + "%–" + to.stripTrailingZeros().toPlainString() + "%";
    }
}