package pe.edu.utec.labreserve.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidSlotTimeException extends RuntimeException {
    public InvalidSlotTimeException(String message) {
        super(message);
    }
}
