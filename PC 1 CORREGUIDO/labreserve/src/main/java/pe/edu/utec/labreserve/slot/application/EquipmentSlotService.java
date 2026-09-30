package pe.edu.utec.labreserve.slot.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utec.labreserve.exception.*;
import pe.edu.utec.labreserve.laboratory.application.LaboratoryService;
import pe.edu.utec.labreserve.laboratory.domain.Laboratory;
import pe.edu.utec.labreserve.laboratory.domain.LaboratoryStatus;
import pe.edu.utec.labreserve.laboratory.infrastructure.LaboratoryRepository;
import pe.edu.utec.labreserve.slot.domain.EquipmentSlot;
import pe.edu.utec.labreserve.slot.domain.SlotStatus;
import pe.edu.utec.labreserve.slot.dto.EquipmentSlotRequestDTO;
import pe.edu.utec.labreserve.slot.dto.EquipmentSlotResponseDTO;
import pe.edu.utec.labreserve.slot.infrastructure.EquipmentSlotRepository;
import pe.edu.utec.labreserve.user.domain.User;

import java.time.ZonedDateTime;

import static pe.edu.utec.labreserve.slot.infrastructure.EquipmentSlotSpecifications.*;

@Service
public class EquipmentSlotService {

    private final EquipmentSlotRepository slotRepository;
    private final LaboratoryRepository laboratoryRepository;

    public EquipmentSlotService(EquipmentSlotRepository slotRepository,
                                LaboratoryRepository laboratoryRepository) {
        this.slotRepository = slotRepository;
        this.laboratoryRepository = laboratoryRepository;
    }

    @Transactional
    public EquipmentSlotResponseDTO create(Long labId, EquipmentSlotRequestDTO request, User currentUser) {
        Laboratory lab = laboratoryRepository.findById(labId)
                .orElseThrow(() -> new LaboratoryNotFoundException("No existe el laboratorio " + labId));

        LaboratoryService.checkOwnership(lab, currentUser);

        if (lab.getStatus() != LaboratoryStatus.ACTIVE) {
            throw new LaboratoryNotActiveException("El laboratorio '" + lab.getName() + "' no está ACTIVE");
        }
        if (!request.getStartTime().isAfter(ZonedDateTime.now())) {
            throw new InvalidSlotTimeException("El startTime debe ser futuro");
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new InvalidSlotTimeException("El startTime debe ser menor que el endTime");
        }
        String code = request.getEquipmentCode().trim();
        if (slotRepository.existsOverlap(code, request.getStartTime(), request.getEndTime(), SlotStatus.CANCELLED)) {
            throw new SlotOverlapException("Ya existe un turno para el equipo " + code + " que se solapa con ese horario");
        }

        EquipmentSlot slot = EquipmentSlot.builder()
                .laboratory(lab)
                .equipmentCode(code)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .capacity(request.getCapacity())
                .status(SlotStatus.AVAILABLE)
                .build();

        return toDto(slotRepository.save(slot));
    }

    @Transactional(readOnly = true)
    public Page<EquipmentSlotResponseDTO> search(Long laboratoryId, String equipmentCode,
                                                 ZonedDateTime from, Pageable pageable) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime lowerBound = (from != null && from.isAfter(now)) ? from : now;

        Specification<EquipmentSlot> spec = Specification.allOf(
                hasStatus(SlotStatus.AVAILABLE),
                startsAfter(lowerBound),
                inLaboratory(laboratoryId),
                equipmentCodeLike(equipmentCode));

        Pageable effective = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("startTime").ascending());

        return slotRepository.findAll(spec, effective).map(this::toDto);
    }

    private EquipmentSlotResponseDTO toDto(EquipmentSlot slot) {
        return new EquipmentSlotResponseDTO(slot.getId(), slot.getLaboratory().getName(),
                slot.getEquipmentCode(), slot.getStatus().name());
    }
}
