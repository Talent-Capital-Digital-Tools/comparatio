package talentcapitalme.com.comparatio.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import talentcapitalme.com.comparatio.service.BulkService;
import talentcapitalme.com.comparatio.service.CompensationService;

import java.io.IOException;

@RestController
@RequestMapping("/api/calc")
@RequiredArgsConstructor
public class CalcController {
    private final CompensationService service;
    private final BulkService bulkService;
    private final CalculationResultRepository resultRepo;

    @PostMapping("/individual")
    public CalcResponse calc(@Valid @RequestBody CalcRequest req) { return service.calculate(req); }

    @PostMapping(value="/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BulkResponse bulk(@RequestPart("file") MultipartFile file) throws IOException { return bulkService.process(file); }

    @GetMapping("/bulk/{batchId}")
    public ResponseEntity<byte[]> download(@PathVariable String batchId) throws IOException {
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

        byte[] xlsx = bulkService.exportExcel(rows);
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        h.setContentDisposition(ContentDisposition.attachment().filename("bulk-results-"+batchId+".xlsx").build());
        return new ResponseEntity<>(xlsx, h, HttpStatus.OK);
    }
}
