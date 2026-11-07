package es.uclm.esi.iso2.sescam.common.dto;

import java.time.LocalDateTime;

/**
 * Criterios opcionales de consulta de notificaciones para un usuario.
 */
public class FiltroNotificacionDTO {

    private String usuarioId;
    private Boolean leida;
    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;

    public FiltroNotificacionDTO() {}

    public FiltroNotificacionDTO(
            String usuarioId, Boolean leida, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        this.usuarioId = usuarioId;
        this.leida = leida;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public Boolean getLeida() { return leida; }
    public void setLeida(Boolean leida) { this.leida = leida; }

    public LocalDateTime getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(LocalDateTime fechaDesde) { this.fechaDesde = fechaDesde; }

    public LocalDateTime getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(LocalDateTime fechaHasta) { this.fechaHasta = fechaHasta; }
}
