package pe.edu.utec.labreserve.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, String message, HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(ErrorResponseDTO.of(status.value(), status.getReasonPhrase(), message, req.getRequestURI()));
    }

    @ExceptionHandler({UserAlreadyExistsException.class, SlotUnavailableException.class,
            ReservationOverlapException.class, SlotOverlapException.class,
            LaboratoryNotActiveException.class, LaboratoryAlreadyExistsException.class})
    public ResponseEntity<ErrorResponseDTO> conflict(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> unauthorized(InvalidCredentialsException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    @ExceptionHandler({LaboratoryNotFoundException.class, EquipmentSlotNotFoundException.class})
    public ResponseEntity<ErrorResponseDTO> notFound(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(ForbiddenLaboratoryActionException.class)
    public ResponseEntity<ErrorResponseDTO> forbidden(ForbiddenLaboratoryActionException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> accessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Rol insuficiente para esta acción", req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        ErrorResponseDTO body = ErrorResponseDTO.of(400, "Bad Request", "Error de validación", req.getRequestURI());
        body.setFieldErrors(errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({InvalidSlotTimeException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponseDTO> badRequest(Exception ex, HttpServletRequest req) {
        String msg = ex instanceof HttpMessageNotReadableException
                ? "El cuerpo de la petición es inválido o está mal formado"
                : ex.getMessage();
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> integrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Se violó una restricción única de la base de datos", req);
    }
}
