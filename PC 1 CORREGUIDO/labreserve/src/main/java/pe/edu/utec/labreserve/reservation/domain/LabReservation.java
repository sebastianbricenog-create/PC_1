package pe.edu.utec.labreserve.reservation.domain;

import jakarta.persistence.*;
import lombok.*;
import pe.edu.utec.labreserve.slot.domain.EquipmentSlot;
import pe.edu.utec.labreserve.user.domain.User;

import java.time.ZonedDateTime;

@Entity
@Table(name = "lab_reservations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reservation_slot_student",
                columnNames = {"slot_id", "student_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false)
    private EquipmentSlot slot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false, length = 250)
    private String purpose;

    @Column(name = "reserved_at", nullable = false)
    private ZonedDateTime reservedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;
}
