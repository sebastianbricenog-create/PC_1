package pe.edu.utec.labreserve.reservation.application;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.utec.labreserve.common.PagedResponseDTO;
import pe.edu.utec.labreserve.reservation.dto.MyReservationDTO;
import pe.edu.utec.labreserve.reservation.dto.ReservationRequestDTO;
import pe.edu.utec.labreserve.reservation.dto.ReservationResponseDTO;
import pe.edu.utec.labreserve.user.domain.User;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/equipment-slots/{slotId}/reservations")
    public ResponseEntity<ReservationResponseDTO> reserve(@PathVariable Long slotId,
                                                          @Valid @RequestBody ReservationRequestDTO request,
                                                          @AuthenticationPrincipal User student) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.reserve(slotId, request, student));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my-lab-reservations")
    public ResponseEntity<PagedResponseDTO<MyReservationDTO>> myReservations(
            @RequestParam(defaultValue = "all") String status,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new PagedResponseDTO<>(reservationService.myReservations(user, status, pageable)));
    }
}
