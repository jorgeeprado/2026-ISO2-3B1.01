package es.uclm.esi.iso2.sescam.notificaciones.dominio;

public class NotificacionDispatchException extends RuntimeException {

    private final String notificacionId;
    private final EstadoEnvio estadoEnvio;

    public NotificacionDispatchException(String notificacionId, EstadoEnvio estadoEnvio, Throwable cause) {
        super("No se pudo entregar la notificación " + notificacionId + " a todos los canales", cause);
        this.notificacionId = notificacionId;
        this.estadoEnvio = estadoEnvio;
    }

    public String getNotificacionId() {
        return notificacionId;
    }

    public EstadoEnvio getEstadoEnvio() {
        return estadoEnvio;
    }
}
