package pe.edu.utec.labreserve.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyReservationDTO {
    private Long id;
    private String equipmentCode;
    private String status;
}
