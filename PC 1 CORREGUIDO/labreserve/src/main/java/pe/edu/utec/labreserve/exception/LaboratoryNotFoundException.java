package pe.edu.utec.labreserve.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class LaboratoryNotFoundException extends RuntimeException {
    public LaboratoryNotFoundException(String message) {
        super(message);
    }
}
