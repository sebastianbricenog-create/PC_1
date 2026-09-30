package pe.edu.utec.labreserve.slot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import pe.edu.utec.labreserve.laboratory.domain.Laboratory;
import pe.edu.utec.labreserve.laboratory.domain.LaboratoryStatus;
import pe.edu.utec.labreserve.reservation.domain.LabReservation;
import pe.edu.utec.labreserve.reservation.domain.ReservationStatus;
import pe.edu.utec.labreserve.reservation.infrastructure.LabReservationRepository;
import pe.edu.utec.labreserve.slot.domain.EquipmentSlot;
import pe.edu.utec.labreserve.slot.domain.SlotStatus;
import pe.edu.utec.labreserve.slot.infrastructure.EquipmentSlotRepository;
import pe.edu.utec.labreserve.user.domain.Role;
import pe.edu.utec.labreserve.user.domain.User;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class EquipmentSlotRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EquipmentSlotRepository slotRepository;

    @Autowired
    private LabReservationRepository reservationRepository;

    private Laboratory lab;
    private User student;
    private ZonedDateTime base;

    @BeforeEach
    void setUp() {
        User tech = em.persist(User.builder().username("tech").email("tech@utec.edu.pe")
                .password("x").role(Role.ROLE_TECHNICIAN).build());
        student = em.persist(User.builder().username("raul.lab").email("raul@utec.edu.pe")
                .password("x").role(Role.ROLE_STUDENT).build());
        lab = em.persist(Laboratory.builder().name("FabLab").location("A-301")
                .manager(tech).status(LaboratoryStatus.ACTIVE).build());

        base = ZonedDateTime.now().plusDays(3).truncatedTo(ChronoUnit.HOURS);
        em.persist(slot("IMP-3D-04", base, base.plusHours(2)));
        em.persist(slot("IMP-3D-05", base, base.plusHours(2)));
        em.persist(slot("CNC-01", base, base.plusHours(1)));
        em.flush();
    }

    private EquipmentSlot slot(String code, ZonedDateTime start, ZonedDateTime end) {
        return EquipmentSlot.builder().laboratory(lab).equipmentCode(code)
                .startTime(start).endTime(end).capacity(1).status(SlotStatus.AVAILABLE).build();
    }

    @Test
    @DisplayName("Búsqueda parcial por equipo (ignore case)")
    void findByEquipmentCodeContaining_returnsOnlyMatches() {
        Page<EquipmentSlot> page = slotRepository
                .findByEquipmentCodeContainingIgnoreCase("3d", PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(EquipmentSlot::getEquipmentCode)
                .containsExactlyInAnyOrder("IMP-3D-04", "IMP-3D-05");
    }

    @Test
    @DisplayName("Detección de solapamiento de turnos por equipo")
    void existsOverlap_detectsCrossingIntervals() {
        assertThat(slotRepository.existsOverlap("IMP-3D-04", base.plusHours(1), base.plusHours(3), SlotStatus.CANCELLED))
                .isTrue();
        assertThat(slotRepository.existsOverlap("IMP-3D-04", base.plusHours(2), base.plusHours(4), SlotStatus.CANCELLED))
                .isFalse();
    }

    @Test
    @DisplayName("Restricción única: mismo equipo y mismo startTime")
    void uniqueConstraint_equipmentAndStartTime() {
        assertThatThrownBy(() -> slotRepository.saveAndFlush(slot("IMP-3D-04", base, base.plusHours(1))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Restricción única: un estudiante no puede reservar dos veces el mismo turno")
    void uniqueConstraint_reservationSlotStudent() {
        EquipmentSlot s = slotRepository.findByEquipmentCodeContainingIgnoreCase("CNC", PageRequest.of(0, 1))
                .getContent().get(0);

        reservationRepository.saveAndFlush(reservation(s));

        assertThatThrownBy(() -> reservationRepository.saveAndFlush(reservation(s)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private LabReservation reservation(EquipmentSlot s) {
        return LabReservation.builder().slot(s).student(student).purpose("Prueba")
                .reservedAt(ZonedDateTime.now()).status(ReservationStatus.RESERVED).build();
    }
}
