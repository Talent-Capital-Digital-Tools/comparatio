package talentcapitalme.com.comparatio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import talentcapitalme.com.comparatio.entity.User;
import talentcapitalme.com.comparatio.enumeration.UserRole;
import talentcapitalme.com.comparatio.service.UserManagementService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test Controller
 * 
 * Purpose: Development and testing utilities for multi-tenant functionality
 * - Test data creation and setup
 * - Multi-tenant isolation verification
 * - Development environment testing
 * - WARNING: This controller should be removed in production
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@Tag(name = "Testing", description = "System health and testing endpoints")
public class TestController {

    @Autowired
    private UserManagementService userManagementService;

    @Operation(summary = "Setup Test Clients", description = "Create sample CLIENT_ADMIN users for testing (Development only)")
    @PostMapping("/setup-clients")
    public ResponseEntity<String> setupTestClients() {
        log.info("Test Controller: Setting up test CLIENT_ADMIN users for development testing");
        try {
            // Create test CLIENT_ADMIN users
            User client1 = new User();
            client1.setUsername("hr_admin");
            client1.setEmail("hr@test.com");
            client1.setFullName("HR Department Admin");
            client1.setPasswordHash("password123"); // In real app, this would be properly hashed
            client1.setRole(UserRole.CLIENT_ADMIN);
            client1.setName("HR Department");
            client1.setActive(true);
            
            User client2 = new User();
            client2.setUsername("finance_admin");
            client2.setEmail("finance@test.com");
            client2.setFullName("Finance Department Admin");
            client2.setPasswordHash("password123"); // In real app, this would be properly hashed
            client2.setRole(UserRole.CLIENT_ADMIN);
            client2.setName("Finance Department");
            client2.setActive(true);
            
            log.info("Test Controller: Creating test client 1: HR Department");
            User savedClient1 = userManagementService.createClientAdmin(client1);
            log.info("Test Controller: Creating test client 2: Finance Department");
            User savedClient2 = userManagementService.createClientAdmin(client2);
            
            log.info("Test Controller: Successfully created {} test CLIENT_ADMIN users with default matrices", 2);
            return ResponseEntity.ok(
                "Successfully created test CLIENT_ADMIN users:\n" +
                "1. " + savedClient1.getName() + " (ID: " + savedClient1.getId() + ")\n" +
                "2. " + savedClient2.getName() + " (ID: " + savedClient2.getId() + ")\n" +
                "Each CLIENT_ADMIN now has its own set of default matrices."
            );
        } catch (Exception e) {
            log.error("Test Controller: Error creating test CLIENT_ADMIN users: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error creating CLIENT_ADMIN users: " + e.getMessage());
        }
    }
    
    @Operation(summary = "List Test Clients", description = "List all CLIENT_ADMIN users for testing")
    @GetMapping("/clients")
    public ResponseEntity<List<User>> listClients() {
        log.info("Test Controller: Listing all CLIENT_ADMIN users for testing purposes");
        List<User> clients = userManagementService.getAllClientAdmins();
        log.info("Test Controller: Found {} CLIENT_ADMIN users", clients.size());
        return ResponseEntity.ok(clients);
    }

    @Operation(summary = "Test Excel Upload", description = "Test Excel file upload with detailed error reporting")
    @PostMapping(value = "/excel-test", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> testExcelUpload(@RequestPart("file") MultipartFile file) {
        log.info("Test Controller: Testing Excel file upload: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
        
        Map<String, Object> result = new HashMap<>();
        result.put("filename", file.getOriginalFilename());
        result.put("size", file.getSize());
        result.put("contentType", file.getContentType());
        
        try {
            // Test basic file reading
            try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
                Sheet sheet = wb.getSheetAt(0);
                result.put("sheetName", sheet.getSheetName());
                result.put("lastRowNum", sheet.getLastRowNum());
                
                // Test reading first few rows
                List<Map<String, Object>> sampleRows = new ArrayList<>();
                for (int i = 0; i <= Math.min(2, sheet.getLastRowNum()); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        Map<String, Object> rowData = new HashMap<>();
                        for (int j = 0; j < 6; j++) {
                            Cell cell = row.getCell(j);
                            if (cell != null) {
                                try {
                                    switch (cell.getCellType()) {
                                        case STRING:
                                            rowData.put("col" + j, cell.getStringCellValue());
                                            break;
                                        case NUMERIC:
                                            rowData.put("col" + j, cell.getNumericCellValue());
                                            break;
                                        case BOOLEAN:
                                            rowData.put("col" + j, cell.getBooleanCellValue());
                                            break;
                                        default:
                                            rowData.put("col" + j, "UNSUPPORTED_TYPE_" + cell.getCellType());
                                    }
                                } catch (Exception e) {
                                    rowData.put("col" + j, "ERROR: " + e.getMessage());
                                }
                            }
                        }
                        sampleRows.add(rowData);
                    }
                }
                result.put("sampleRows", sampleRows);
                result.put("status", "SUCCESS");
            }
        } catch (Exception e) {
            log.error("Test Controller: Error testing Excel file", e);
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
        }
        
        return ResponseEntity.ok(result);
    }
}