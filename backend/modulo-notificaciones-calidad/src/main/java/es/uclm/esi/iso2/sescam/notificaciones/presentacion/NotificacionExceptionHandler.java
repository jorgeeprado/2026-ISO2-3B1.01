package es.uclm.esi.iso2.sescam.notificaciones.presentacion;

import es.uclm.esi.iso2.sescam.notificaciones.dominio.NotificacionNoEncontradaException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class NotificacionExceptionHandler {

    @ExceptionHandler(NotificacionNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> notificacionNoEncontrada(
            NotificacionNoEncontradaException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argumentoInvalido(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }
}
