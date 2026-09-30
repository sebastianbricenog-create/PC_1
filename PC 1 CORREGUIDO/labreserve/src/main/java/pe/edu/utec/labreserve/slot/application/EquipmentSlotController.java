package pe.edu.utec.labreserve.slot.application;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.utec.labreserve.common.PagedResponseDTO;
import pe.edu.utec.labreserve.slot.dto.EquipmentSlotRequestDTO;
import pe.edu.utec.labreserve.slot.dto.EquipmentSlotResponseDTO;
import pe.edu.utec.labreserve.user.domain.User;

import java.time.ZonedDateTime;

@RestController
public class EquipmentSlotController {

    private final EquipmentSlotService slotService;

    public EquipmentSlotController(EquipmentSlotService slotService) {
        this.slotService = slotService;
    }

    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN')")
    @PostMapping("/laboratories/{labId}/slots")
    public ResponseEntity<EquipmentSlotResponseDTO> create(@PathVariable Long labId,
                                                           @Valid @RequestBody EquipmentSlotRequestDTO request,
                                                           @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(slotService.create(labId, request, user));
    }

    @GetMapping("/equipment-slots")
    public ResponseEntity<PagedResponseDTO<EquipmentSlotResponseDTO>> search(
            @RequestParam(required = false) Long laboratoryId,
            @RequestParam(required = false) String equipmentCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return ResponseEntity.ok(new PagedResponseDTO<>(
                slotService.search(laboratoryId, equipmentCode, from, pageable)));
    }
}
