package pe.edu.utec.labreserve.laboratory.application;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.utec.labreserve.common.PagedResponseDTO;
import pe.edu.utec.labreserve.laboratory.dto.LaboratoryRequestDTO;
import pe.edu.utec.labreserve.laboratory.dto.LaboratoryResponseDTO;
import pe.edu.utec.labreserve.laboratory.dto.LaboratoryStatusRequestDTO;
import pe.edu.utec.labreserve.user.domain.User;

@RestController
@RequestMapping("/laboratories")
public class LaboratoryController {

    private final LaboratoryService laboratoryService;

    public LaboratoryController(LaboratoryService laboratoryService) {
        this.laboratoryService = laboratoryService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<LaboratoryResponseDTO> create(@Valid @RequestBody LaboratoryRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(laboratoryService.create(request));
    }

    @GetMapping
    public ResponseEntity<PagedResponseDTO<LaboratoryResponseDTO>> findAll(
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return ResponseEntity.ok(new PagedResponseDTO<>(laboratoryService.findAll(pageable)));
    }

    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN')")
    @PatchMapping("/{labId}/status")
    public ResponseEntity<LaboratoryResponseDTO> changeStatus(@PathVariable Long labId,
                                                              @Valid @RequestBody LaboratoryStatusRequestDTO request,
                                                              @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(laboratoryService.changeStatus(labId, request.getStatus(), user));
    }
}
