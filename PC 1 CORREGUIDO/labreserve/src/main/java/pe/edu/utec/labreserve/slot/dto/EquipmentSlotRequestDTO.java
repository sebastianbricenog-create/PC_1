package pe.edu.utec.labreserve.slot.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentSlotRequestDTO {

    @NotBlank(message = "El equipmentCode es obligatorio")
    @Size(max = 50)
    private String equipmentCode;

    @NotNull(message = "El startTime es obligatorio")
    @Future(message = "El startTime debe ser una fecha futura")
    private ZonedDateTime startTime;

    @NotNull(message = "El endTime es obligatorio")
    @Future(message = "El endTime debe ser una fecha futura")
    private ZonedDateTime endTime;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad mínima es 1")
    private Integer capacity;
}
