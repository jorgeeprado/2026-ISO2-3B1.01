package es.uclm.esi.iso2.sescam.notificaciones.dominio;

public class NotificacionNoEncontradaException extends RuntimeException {

    public NotificacionNoEncontradaException(Long notificacionId) {
        super("No existe la notificación con id " + notificacionId);
    }
}
