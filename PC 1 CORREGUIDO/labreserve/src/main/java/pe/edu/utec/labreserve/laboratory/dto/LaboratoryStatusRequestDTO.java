package pe.edu.utec.labreserve.laboratory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.utec.labreserve.laboratory.domain.LaboratoryStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaboratoryStatusRequestDTO {
    @NotNull(message = "El status es obligatorio (ACTIVE | MAINTENANCE)")
    private LaboratoryStatus status;
}
