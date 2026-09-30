package pe.edu.utec.labreserve.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenLaboratoryActionException extends RuntimeException {
    public ForbiddenLaboratoryActionException(String message) {
        super(message);
    }
}
