package pe.edu.utec.labreserve.laboratory.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utec.labreserve.laboratory.domain.Laboratory;

public interface LaboratoryRepository extends JpaRepository<Laboratory, Long> {
    boolean existsByName(String name);
}
