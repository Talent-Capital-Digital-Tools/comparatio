package talentcapitalme.com.comparatio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Template Service
 * 
 * Purpose: Handles Excel template generation for bulk uploads
 * - Creates standardized Excel templates with pre-set columns
 * - Ensures consistent data format for bulk processing
 * - Provides user-friendly template with clear instructions
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService implements ITemplateService {

    /**
     * Generate Excel template for bulk compensation calculation upload
     * 
     * @return Excel file as byte array
     * @throws IOException if template generation fails
     */
    public byte[] generateBulkUploadTemplate() throws IOException {
        log.info("Generating bulk upload template");
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Compensation Upload Template");
            
            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle instructionStyle = createInstructionStyle(workbook);
            
            // Create headers row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Employee Code",
                "Employee Name", 
                "Job Title",
                "Years of Experience",
                "Performance Rating",
                "Current Salary",
                "Mid of Scale"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Add instruction rows
            addInstructions(sheet, instructionStyle);
            
            // Add sample data row
            addSampleData(sheet);
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Convert to byte array
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                log.info("Template generated successfully with {} columns", headers.length);
                return outputStream.toByteArray();
            }
        }
    }
    
    /**
     * Create header cell style
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    /**
     * Create instruction cell style
     */
    private CellStyle createInstructionStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setItalic(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    /**
     * Add instruction rows to the template
     */
    private void addInstructions(Sheet sheet, CellStyle instructionStyle) {
        String[] instructions = {
            "INSTRUCTIONS:",
            "1. Fill in the data for each employee in the rows below",
            "2. Employee Code: Unique identifier for the employee",
            "3. Employee Name: Full name of the employee",
            "4. Job Title: Current job title/position",
            "5. Years of Experience: Total years of work experience (whole number)",
            "6. Performance Rating: Rating from 1-5 (1=Below Expectations, 5=Exceeds Expectations)",
            "7. Current Salary: Employee's current annual salary (numbers only, no currency symbols)",
            "8. Mid of Scale: Mid-point of the salary range for this position (numbers only)",
            "9. Do not modify the header row or column structure",
            "10. Save the file as .xlsx format before uploading"
        };
        
        for (int i = 0; i < instructions.length; i++) {
            Row row = sheet.createRow(i + 1);
            Cell cell = row.createCell(0);
            cell.setCellValue(instructions[i]);
            cell.setCellStyle(instructionStyle);
            
            // Merge cells for instruction text
            if (i > 0) {
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(i + 1, i + 1, 0, 6));
            }
        }
    }
    
    /**
     * Add sample data row
     */
    private void addSampleData(Sheet sheet) {
        int sampleRowIndex = 13; // After instructions
        Row sampleRow = sheet.createRow(sampleRowIndex);
        
        Object[] sampleData = {
            "EMP001",
            "John Smith",
            "Software Engineer",
            5,
            4,
            75000,
            80000
        };
        
        for (int i = 0; i < sampleData.length; i++) {
            Cell cell = sampleRow.createCell(i);
            if (sampleData[i] instanceof String) {
                cell.setCellValue((String) sampleData[i]);
            } else if (sampleData[i] instanceof Integer) {
                cell.setCellValue((Integer) sampleData[i]);
            }
        }
        
        // Add sample label
        Row labelRow = sheet.createRow(sampleRowIndex - 1);
        Cell labelCell = labelRow.createCell(0);
        labelCell.setCellValue("SAMPLE DATA (delete this row before uploading):");
        CellStyle labelStyle = sheet.getWorkbook().createCellStyle();
        Font labelFont = sheet.getWorkbook().createFont();
        labelFont.setBold(true);
        labelFont.setFontHeightInPoints((short) 10);
        labelStyle.setFont(labelFont);
        labelCell.setCellStyle(labelStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(sampleRowIndex - 1, sampleRowIndex - 1, 0, 6));
    }
}
