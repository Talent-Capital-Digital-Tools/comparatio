package talentcapitalme.com.comparatio.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import talentcapitalme.com.comparatio.entity.AdjustmentMatrix;
import talentcapitalme.com.comparatio.exception.NotFoundException;
import talentcapitalme.com.comparatio.repository.AdjustmentMatrixRepository;
import talentcapitalme.com.comparatio.security.Authz;

import java.util.List;

@RestController
@RequestMapping("/api/matrix")
@RequiredArgsConstructor
public class MatrixController {
    private final AdjustmentMatrixRepository repo;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CLIENT_ADMIN')")
    public List<AdjustmentMatrix> list(@RequestParam(required = false) String clientId) {
        String cid = Authz.requireClientScope(clientId);
        return repo.findByClientId(cid);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CLIENT_ADMIN')")
    public AdjustmentMatrix create(@RequestParam(required = false) String clientId,
                                   @RequestBody AdjustmentMatrix m) {
        String cid = Authz.requireClientScope(clientId);
        m.setClientId(cid);
        return repo.save(m);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CLIENT_ADMIN')")
    public AdjustmentMatrix update(@PathVariable String id,
                                   @RequestParam(required = false) String clientId,
                                   @RequestBody AdjustmentMatrix m) {
        String cid = Authz.requireClientScope(clientId);
        m.setId(id);
        m.setClientId(cid);
        return repo.save(m);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CLIENT_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id,
                       @RequestParam(required = false) String clientId) {
        String cid = Authz.requireClientScope(clientId);
        repo.findById(id).filter(x -> cid.equals(x.getClientId()))
                .ifPresentOrElse(x -> repo.deleteById(id), () -> { 
                    throw new NotFoundException("Adjustment matrix not found or you don't have permission to delete it"); 
                });
    }
}
