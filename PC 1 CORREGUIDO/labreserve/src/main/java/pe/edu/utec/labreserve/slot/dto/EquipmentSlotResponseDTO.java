package pe.edu.utec.labreserve.slot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EquipmentSlotResponseDTO {
    private Long id;
    private String laboratoryName;
    private String equipmentCode;
    private String status;
}
