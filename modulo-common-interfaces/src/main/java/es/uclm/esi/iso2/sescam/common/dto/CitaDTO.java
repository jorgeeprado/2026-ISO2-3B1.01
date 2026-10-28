package es.uclm.esi.iso2.sescam.common.dto;

import es.uclm.esi.iso2.sescam.common.enums.EstadoCita;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO que representa una cita programada en la agenda del servicio (RF-01, RF-13).
 */
public class CitaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String solicitudId;
    private String pacienteId;
    private String tipoPruebaId;
    private LocalDateTime fechaHora;
    private int duracionMinutos;
    private String salaId;
    private String medicoRadiologoId;
    private EstadoCita estado;
    private String motivoCancelacion;

    public CitaDTO() {}

    public CitaDTO(String id, String solicitudId, String pacienteId, String tipoPruebaId,
                   LocalDateTime fechaHora, int duracionMinutos, String salaId,
                   String medicoRadiologoId, EstadoCita estado, String motivoCancelacion) {
        this.id = id;
        this.solicitudId = solicitudId;
        this.pacienteId = pacienteId;
        this.tipoPruebaId = tipoPruebaId;
        this.fechaHora = fechaHora;
        this.duracionMinutos = duracionMinutos;
        this.salaId = salaId;
        this.medicoRadiologoId = medicoRadiologoId;
        this.estado = estado;
        this.motivoCancelacion = motivoCancelacion;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSolicitudId() { return solicitudId; }
    public void setSolicitudId(String solicitudId) { this.solicitudId = solicitudId; }

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }

    public String getTipoPruebaId() { return tipoPruebaId; }
    public void setTipoPruebaId(String tipoPruebaId) { this.tipoPruebaId = tipoPruebaId; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public int getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(int duracionMinutos) { this.duracionMinutos = duracionMinutos; }

    public String getSalaId() { return salaId; }
    public void setSalaId(String salaId) { this.salaId = salaId; }

    public String getMedicoRadiologoId() { return medicoRadiologoId; }
    public void setMedicoRadiologoId(String medicoRadiologoId) { this.medicoRadiologoId = medicoRadiologoId; }

    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }

    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }
}
