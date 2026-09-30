package pe.edu.utec.labreserve.reservation.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.utec.labreserve.reservation.domain.LabReservation;
import pe.edu.utec.labreserve.reservation.domain.ReservationStatus;

import java.time.ZonedDateTime;

public interface LabReservationRepository extends JpaRepository<LabReservation, Long> {

    boolean existsBySlot_IdAndStudent_Id(Long slotId, Long studentId);

    @Query("""
            select case when count(r) > 0 then true else false end
            from LabReservation r
            where r.student.id = :studentId
              and r.status = :status
              and r.slot.startTime < :endTime
              and r.slot.endTime > :startTime
            """)
    boolean existsOverlappingReservation(@Param("studentId") Long studentId,
                                         @Param("startTime") ZonedDateTime startTime,
                                         @Param("endTime") ZonedDateTime endTime,
                                         @Param("status") ReservationStatus status);

    @EntityGraph(attributePaths = "slot")
    Page<LabReservation> findByStudent_Id(Long studentId, Pageable pageable);

    @EntityGraph(attributePaths = "slot")
    Page<LabReservation> findByStudent_IdAndStatus(Long studentId, ReservationStatus status, Pageable pageable);
}
