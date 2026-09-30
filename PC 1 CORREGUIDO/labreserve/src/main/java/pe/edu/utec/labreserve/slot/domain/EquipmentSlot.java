package pe.edu.utec.labreserve.slot.domain;

import jakarta.persistence.*;
import lombok.*;
import pe.edu.utec.labreserve.laboratory.domain.Laboratory;

import java.time.ZonedDateTime;

@Entity
@Table(name = "equipment_slots",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_slot_equipment_start",
                columnNames = {"equipment_code", "start_time"}),
        indexes = @Index(name = "idx_slot_equipment_code", columnList = "equipment_code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "laboratory_id", nullable = false)
    private Laboratory laboratory;

    @Column(name = "equipment_code", nullable = false, length = 50)
    private String equipmentCode;

    @Column(name = "start_time", nullable = false)
    private ZonedDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private ZonedDateTime endTime;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SlotStatus status;

    @Version
    private Long version;
}
