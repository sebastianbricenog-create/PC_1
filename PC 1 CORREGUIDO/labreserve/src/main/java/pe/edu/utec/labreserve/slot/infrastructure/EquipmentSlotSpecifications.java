package pe.edu.utec.labreserve.slot.infrastructure;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import pe.edu.utec.labreserve.slot.domain.EquipmentSlot;
import pe.edu.utec.labreserve.slot.domain.SlotStatus;

import java.time.ZonedDateTime;

public final class EquipmentSlotSpecifications {

    private EquipmentSlotSpecifications() {}

    public static Specification<EquipmentSlot> hasStatus(SlotStatus status) {
        return (root, q, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<EquipmentSlot> startsAfter(ZonedDateTime from) {
        return (root, q, cb) -> cb.greaterThan(root.get("startTime"), from);
    }

    public static Specification<EquipmentSlot> inLaboratory(Long laboratoryId) {
        return (root, q, cb) -> laboratoryId == null
                ? null
                : cb.equal(root.get("laboratory").get("id"), laboratoryId);
    }

    public static Specification<EquipmentSlot> equipmentCodeLike(String code) {
        return (root, q, cb) -> !StringUtils.hasText(code)
                ? null
                : cb.like(cb.lower(root.get("equipmentCode")), "%" + code.trim().toLowerCase() + "%");
    }
}
