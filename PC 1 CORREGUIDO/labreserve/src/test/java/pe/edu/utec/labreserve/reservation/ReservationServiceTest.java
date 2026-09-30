package pe.edu.utec.labreserve.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.utec.labreserve.exception.EquipmentSlotNotFoundException;
import pe.edu.utec.labreserve.exception.ReservationOverlapException;
import pe.edu.utec.labreserve.exception.SlotUnavailableException;
import pe.edu.utec.labreserve.reservation.application.ReservationService;
import pe.edu.utec.labreserve.reservation.domain.LabReservation;
import pe.edu.utec.labreserve.reservation.domain.ReservationStatus;
import pe.edu.utec.labreserve.reservation.dto.ReservationRequestDTO;
import pe.edu.utec.labreserve.reservation.dto.ReservationResponseDTO;
import pe.edu.utec.labreserve.reservation.infrastructure.LabReservationRepository;
import pe.edu.utec.labreserve.slot.domain.EquipmentSlot;
import pe.edu.utec.labreserve.slot.domain.SlotStatus;
import pe.edu.utec.labreserve.slot.infrastructure.EquipmentSlotRepository;
import pe.edu.utec.labreserve.user.domain.Role;
import pe.edu.utec.labreserve.user.domain.User;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private LabReservationRepository reservationRepository;

    @Mock
    private EquipmentSlotRepository slotRepository;

    @InjectMocks
    private ReservationService reservationService;

    private User student;
    private EquipmentSlot slot;
    private final ReservationRequestDTO request = new ReservationRequestDTO("Prototipo del curso de Diseño");

    @BeforeEach
    void setUp() {
        student = User.builder().id(10L).username("raul.lab").email("raul@utec.edu.pe")
                .password("x").role(Role.ROLE_STUDENT).build();

        ZonedDateTime start = ZonedDateTime.now().plusDays(2);
        slot = EquipmentSlot.builder().id(6L).equipmentCode("IMP-3D-04")
                .startTime(start).endTime(start.plusHours(2))
                .capacity(1).status(SlotStatus.AVAILABLE).build();
    }

    @Test
    @DisplayName("Solapamiento: si el estudiante ya tiene una reserva que se cruza, lanza ReservationOverlapException")
    void reserve_whenStudentHasOverlappingReservation_throwsOverlap() {
        when(slotRepository.findByIdForUpdate(6L)).thenReturn(Optional.of(slot));
        when(reservationRepository.existsBySlot_IdAndStudent_Id(6L, 10L)).thenReturn(false);
        when(reservationRepository.existsOverlappingReservation(
                eq(10L), eq(slot.getStartTime()), eq(slot.getEndTime()), eq(ReservationStatus.RESERVED)))
                .thenReturn(true);

        assertThatThrownBy(() -> reservationService.reserve(6L, request, student))
                .isInstanceOf(ReservationOverlapException.class);

        verify(reservationRepository, never()).save(any());
        verify(slotRepository, never()).save(any());
        assertThat(slot.getCapacity()).isEqualTo(1);
    }

    @Test
    @DisplayName("Duplicado: reservar dos veces el mismo turno lanza ReservationOverlapException")
    void reserve_sameSlotTwice_throwsOverlap() {
        when(slotRepository.findByIdForUpdate(6L)).thenReturn(Optional.of(slot));
        when(reservationRepository.existsBySlot_IdAndStudent_Id(6L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.reserve(6L, request, student))
                .isInstanceOf(ReservationOverlapException.class);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Sin solapamiento: crea la reserva, descuenta capacidad y marca FULL al llegar a 0")
    void reserve_withoutOverlap_createsReservationAndUpdatesCapacity() {
        when(slotRepository.findByIdForUpdate(6L)).thenReturn(Optional.of(slot));
        when(reservationRepository.existsBySlot_IdAndStudent_Id(6L, 10L)).thenReturn(false);
        when(reservationRepository.existsOverlappingReservation(any(), any(), any(), any())).thenReturn(false);
        when(reservationRepository.save(any(LabReservation.class))).thenAnswer(inv -> {
            LabReservation r = inv.getArgument(0);
            r.setId(18L);
            return r;
        });

        ReservationResponseDTO response = reservationService.reserve(6L, request, student);

        assertThat(response.getId()).isEqualTo(18L);
        assertThat(response.getSlotId()).isEqualTo(6L);
        assertThat(response.getStudentUsername()).isEqualTo("raul.lab");
        assertThat(response.getStatus()).isEqualTo("RESERVED");
        assertThat(slot.getCapacity()).isZero();
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.FULL);
        verify(slotRepository).save(slot);
    }

    @Test
    @DisplayName("Turno lleno o cancelado lanza SlotUnavailableException")
    void reserve_whenSlotFull_throwsUnavailable() {
        slot.setCapacity(0);
        slot.setStatus(SlotStatus.FULL);
        when(slotRepository.findByIdForUpdate(6L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> reservationService.reserve(6L, request, student))
                .isInstanceOf(SlotUnavailableException.class);
    }

    @Test
    @DisplayName("Turno inexistente lanza EquipmentSlotNotFoundException")
    void reserve_whenSlotMissing_throwsNotFound() {
        when(slotRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.reserve(99L, request, student))
                .isInstanceOf(EquipmentSlotNotFoundException.class);
    }
}
