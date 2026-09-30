package pe.edu.utec.labreserve.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequestDTO {

    @NotBlank(message = "El propósito es obligatorio")
    @Size(max = 250, message = "El propósito admite máximo 250 caracteres")
    private String purpose;
}
