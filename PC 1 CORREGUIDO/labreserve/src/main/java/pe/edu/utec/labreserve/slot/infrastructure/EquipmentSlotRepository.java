package pe.edu.utec.labreserve.slot.infrastructure;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.utec.labreserve.slot.domain.EquipmentSlot;
import pe.edu.utec.labreserve.slot.domain.SlotStatus;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface EquipmentSlotRepository extends JpaRepository<EquipmentSlot, Long>,
        JpaSpecificationExecutor<EquipmentSlot> {

    @Query("""
            select case when count(s) > 0 then true else false end
            from EquipmentSlot s
            where s.equipmentCode = :equipmentCode
              and s.status <> :excluded
              and s.startTime < :endTime
              and s.endTime > :startTime
            """)
    boolean existsOverlap(@Param("equipmentCode") String equipmentCode,
                          @Param("startTime") ZonedDateTime startTime,
                          @Param("endTime") ZonedDateTime endTime,
                          @Param("excluded") SlotStatus excluded);

    Page<EquipmentSlot> findByEquipmentCodeContainingIgnoreCase(String equipmentCode, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from EquipmentSlot s where s.id = :id")
    Optional<EquipmentSlot> findByIdForUpdate(@Param("id") Long id);
}
