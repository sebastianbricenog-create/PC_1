package pe.edu.utec.labreserve.laboratory.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utec.labreserve.exception.ForbiddenLaboratoryActionException;
import pe.edu.utec.labreserve.exception.LaboratoryAlreadyExistsException;
import pe.edu.utec.labreserve.exception.LaboratoryNotFoundException;
import pe.edu.utec.labreserve.laboratory.domain.Laboratory;
import pe.edu.utec.labreserve.laboratory.domain.LaboratoryStatus;
import pe.edu.utec.labreserve.laboratory.dto.LaboratoryRequestDTO;
import pe.edu.utec.labreserve.laboratory.dto.LaboratoryResponseDTO;
import pe.edu.utec.labreserve.laboratory.infrastructure.LaboratoryRepository;
import pe.edu.utec.labreserve.user.domain.Role;
import pe.edu.utec.labreserve.user.domain.User;
import pe.edu.utec.labreserve.user.infrastructure.UserRepository;

@Service
public class LaboratoryService {

    private final LaboratoryRepository laboratoryRepository;
    private final UserRepository userRepository;

    public LaboratoryService(LaboratoryRepository laboratoryRepository, UserRepository userRepository) {
        this.laboratoryRepository = laboratoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public LaboratoryResponseDTO create(LaboratoryRequestDTO request) {
        if (laboratoryRepository.existsByName(request.getName())) {
            throw new LaboratoryAlreadyExistsException("Ya existe un laboratorio con el nombre '" + request.getName() + "'");
        }
        User manager = userRepository.findById(request.getManagerId())
                .orElseThrow(() -> new IllegalArgumentException("No existe el usuario manager " + request.getManagerId()));
        if (manager.getRole() == Role.ROLE_STUDENT) {
            throw new IllegalArgumentException("El manager debe ser ROLE_TECHNICIAN o ROLE_ADMIN");
        }

        Laboratory lab = Laboratory.builder()
                .name(request.getName())
                .location(request.getLocation())
                .manager(manager)
                .status(LaboratoryStatus.ACTIVE)
                .build();
        return toDto(laboratoryRepository.save(lab));
    }

    @Transactional(readOnly = true)
    public Page<LaboratoryResponseDTO> findAll(Pageable pageable) {
        return laboratoryRepository.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public LaboratoryResponseDTO changeStatus(Long labId, LaboratoryStatus status, User currentUser) {
        Laboratory lab = laboratoryRepository.findById(labId)
                .orElseThrow(() -> new LaboratoryNotFoundException("No existe el laboratorio " + labId));
        checkOwnership(lab, currentUser);
        lab.setStatus(status);
        return toDto(lab);
    }

    public static void checkOwnership(Laboratory lab, User user) {
        if (user.getRole() == Role.ROLE_ADMIN) return;
        if (user.getRole() == Role.ROLE_TECHNICIAN && lab.getManager().getId().equals(user.getId())) return;
        throw new ForbiddenLaboratoryActionException("No puedes operar el laboratorio '" + lab.getName() + "': no eres su responsable");
    }

    private LaboratoryResponseDTO toDto(Laboratory lab) {
        return new LaboratoryResponseDTO(lab.getId(), lab.getName(), lab.getLocation(),
                lab.getManager().getId(), lab.getStatus().name());
    }
}
