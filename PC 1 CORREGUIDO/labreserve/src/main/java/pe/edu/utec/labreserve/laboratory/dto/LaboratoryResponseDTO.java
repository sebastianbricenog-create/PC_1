package pe.edu.utec.labreserve.laboratory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaboratoryResponseDTO {
    private Long id;
    private String name;
    private String location;
    private Long managerId;
    private String status;
}
