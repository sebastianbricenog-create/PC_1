package pe.edu.utec.labreserve.reservation.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utec.labreserve.exception.EquipmentSlotNotFoundException;
import pe.edu.utec.labreserve.exception.ReservationOverlapException;
import pe.edu.utec.labreserve.exception.SlotUnavailableException;
import pe.edu.utec.labreserve.reservation.domain.LabReservation;
import pe.edu.utec.labreserve.reservation.domain.ReservationStatus;
import pe.edu.utec.labreserve.reservation.dto.MyReservationDTO;
import pe.edu.utec.labreserve.reservation.dto.ReservationRequestDTO;
import pe.edu.utec.labreserve.reservation.dto.ReservationResponseDTO;
import pe.edu.utec.labreserve.reservation.infrastructure.LabReservationRepository;
import pe.edu.utec.labreserve.slot.domain.EquipmentSlot;
import pe.edu.utec.labreserve.slot.domain.SlotStatus;
import pe.edu.utec.labreserve.slot.infrastructure.EquipmentSlotRepository;
import pe.edu.utec.labreserve.user.domain.User;

import java.time.ZonedDateTime;
import java.util.Locale;

@Service
public class ReservationService {

    private final LabReservationRepository reservationRepository;
    private final EquipmentSlotRepository slotRepository;

    public ReservationService(LabReservationRepository reservationRepository,
                              EquipmentSlotRepository slotRepository) {
        this.reservationRepository = reservationRepository;
        this.slotRepository = slotRepository;
    }

    @Transactional
    public ReservationResponseDTO reserve(Long slotId, ReservationRequestDTO request, User student) {
        EquipmentSlot slot = slotRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> new EquipmentSlotNotFoundException("No existe el turno " + slotId));

        if (slot.getStatus() != SlotStatus.AVAILABLE || slot.getCapacity() <= 0) {
            throw new SlotUnavailableException("El turno " + slotId + " no tiene capacidad o está cancelado");
        }
        if (!slot.getStartTime().isAfter(ZonedDateTime.now())) {
            throw new SlotUnavailableException("El turno " + slotId + " ya inició o terminó");
        }
        if (reservationRepository.existsBySlot_IdAndStudent_Id(slotId, student.getId())) {
            throw new ReservationOverlapException("Ya tienes una reserva para el turno " + slotId);
        }
        if (reservationRepository.existsOverlappingReservation(
                student.getId(), slot.getStartTime(), slot.getEndTime(), ReservationStatus.RESERVED)) {
            throw new ReservationOverlapException("Ya tienes otra reserva que se superpone con este horario");
        }

        LabReservation reservation = LabReservation.builder()
                .slot(slot)
                .student(student)
                .purpose(request.getPurpose().trim())
                .reservedAt(ZonedDateTime.now())
                .status(ReservationStatus.RESERVED)
                .build();

        slot.setCapacity(slot.getCapacity() - 1);
        if (slot.getCapacity() == 0) {
            slot.setStatus(SlotStatus.FULL);
        }
        slotRepository.save(slot);
        LabReservation saved = reservationRepository.save(reservation);

        return new ReservationResponseDTO(saved.getId(), slot.getId(), student.getUsername(), saved.getStatus().name());
    }

    @Transactional(readOnly = true)
    public Page<MyReservationDTO> myReservations(User student, String status, Pageable pageable) {
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "reservedAt"));

        String normalized = status == null ? "all" : status.trim().toLowerCase(Locale.ROOT);
        Page<LabReservation> page;
        if (normalized.equals("all")) {
            page = reservationRepository.findByStudent_Id(student.getId(), sorted);
        } else {
            ReservationStatus st;
            try {
                st = ReservationStatus.valueOf(normalized.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("status debe ser: all | reserved | used | cancelled");
            }
            page = reservationRepository.findByStudent_IdAndStatus(student.getId(), st, sorted);
        }

        return page.map(r -> new MyReservationDTO(r.getId(), r.getSlot().getEquipmentCode(), r.getStatus().name()));
    }
}
