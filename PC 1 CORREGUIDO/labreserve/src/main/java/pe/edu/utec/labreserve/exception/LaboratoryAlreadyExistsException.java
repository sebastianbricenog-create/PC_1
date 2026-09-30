package pe.edu.utec.labreserve.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class LaboratoryAlreadyExistsException extends RuntimeException {
    public LaboratoryAlreadyExistsException(String message) {
        super(message);
    }
}
