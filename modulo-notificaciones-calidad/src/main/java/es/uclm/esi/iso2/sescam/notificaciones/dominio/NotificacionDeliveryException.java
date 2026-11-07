package es.uclm.esi.iso2.sescam.notificaciones.dominio;

public class NotificacionDeliveryException extends Exception {

    public NotificacionDeliveryException(String message) {
        super(message);
    }

    public NotificacionDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
