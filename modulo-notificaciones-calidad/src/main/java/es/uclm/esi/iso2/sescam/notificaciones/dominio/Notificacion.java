package es.uclm.esi.iso2.sescam.notificaciones.dominio;

import es.uclm.esi.iso2.sescam.common.dto.NotificacionDTO;
import java.util.Objects;

public final class Notificacion {

    private final Long notificacionId;
    private final NotificacionDTO dto;
    private final EstadoEnvio estadoEnvio;
    private final int canalesEnviados;
    private final int canalesFallidos;

    public Notificacion(NotificacionDTO dto, EstadoEnvio estadoEnvio, int canalesEnviados, int canalesFallidos) {
        this(dto.getNotificacionId(), dto, estadoEnvio, canalesEnviados, canalesFallidos);
    }

    public Notificacion(
            Long notificacionId, NotificacionDTO dto, EstadoEnvio estadoEnvio,
            int canalesEnviados, int canalesFallidos) {
        if (notificacionId != null && notificacionId <= 0) {
            throw new IllegalArgumentException("El id numérico de notificación debe ser positivo");
        }
        this.notificacionId = notificacionId;
        this.dto = copiar(Objects.requireNonNull(dto, "La notificación no puede ser nula"));
        this.dto.setNotificacionId(notificacionId);
        this.estadoEnvio = Objects.requireNonNull(estadoEnvio, "El estado de envío no puede ser nulo");
        if (canalesEnviados < 0 || canalesFallidos < 0) {
            throw new IllegalArgumentException("Los contadores de canales no pueden ser negativos");
        }
        this.canalesEnviados = canalesEnviados;
        this.canalesFallidos = canalesFallidos;
    }

    public NotificacionDTO getDto() {
        return copiar(dto);
    }

    public Long getNotificacionId() {
        return notificacionId;
    }

    public EstadoEnvio getEstadoEnvio() {
        return estadoEnvio;
    }

    public int getCanalesEnviados() {
        return canalesEnviados;
    }

    public int getCanalesFallidos() {
        return canalesFallidos;
    }

    private static NotificacionDTO copiar(NotificacionDTO origen) {
        return new NotificacionDTO(
                origen.getId(),
                origen.getNotificacionId(),
                origen.getDestinatarioId(),
                origen.getTipo(),
                origen.getTitulo(),
                origen.getMensaje(),
                origen.getFechaEmision(),
                origen.isLeida(),
                origen.getReferenciaEntidadId());
    }
}
