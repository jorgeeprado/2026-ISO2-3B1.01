package es.uclm.esi.iso2.sescam.notificaciones.presentacion;

import es.uclm.esi.iso2.sescam.notificaciones.dominio.NotificacionDispatchException;
import es.uclm.esi.iso2.sescam.notificaciones.dominio.NotificacionNoEncontradaException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = NotificacionController.class)
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

    /**
     * Algún canal no entregó la notificación: queda persistida como PARCIAL o FALLIDA (D-009).
     */
    @ExceptionHandler(NotificacionDispatchException.class)
    public ResponseEntity<Map<String, String>> despachoFallido(NotificacionDispatchException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "error", exception.getMessage(),
                "notificacionId", exception.getNotificacionId(),
                "estadoEnvio", exception.getEstadoEnvio().name()));
    }

    /**
     * No hay canales de notificación configurados en la aplicación integradora (D-010).
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> servicioNoDisponible(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", exception.getMessage()));
    }
}
