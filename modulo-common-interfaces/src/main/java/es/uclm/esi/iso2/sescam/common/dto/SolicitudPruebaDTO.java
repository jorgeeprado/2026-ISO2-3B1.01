package es.uclm.esi.iso2.sescam.common.dto;

import es.uclm.esi.iso2.sescam.common.enums.EstadoSolicitud;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO que representa una prescripción médica de prueba diagnóstica (RF-06, RF-07).
 */
public class SolicitudPruebaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String pacienteId;
    private String medicoPrescriptorId;
    private String tipoPruebaId;
    private Prioridad prioridad;
    private String indicacionClinica;
    private String justificacionDiagnostica;
    private LocalDateTime fechaSolicitud;
    private EstadoSolicitud estado;
    private boolean esSeguimientoPeriodico;
    private Integer periodicidadMeses;

    public SolicitudPruebaDTO() {}

    public SolicitudPruebaDTO(String id, String pacienteId, String medicoPrescriptorId, String tipoPruebaId,
                              Prioridad prioridad, String indicacionClinica, String justificacionDiagnostica,
                              LocalDateTime fechaSolicitud, EstadoSolicitud estado,
                              boolean esSeguimientoPeriodico, Integer periodicidadMeses) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.medicoPrescriptorId = medicoPrescriptorId;
        this.tipoPruebaId = tipoPruebaId;
        this.prioridad = prioridad;
        this.indicacionClinica = indicacionClinica;
        this.justificacionDiagnostica = justificacionDiagnostica;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = estado;
        this.esSeguimientoPeriodico = esSeguimientoPeriodico;
        this.periodicidadMeses = periodicidadMeses;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }

    public String getMedicoPrescriptorId() { return medicoPrescriptorId; }
    public void setMedicoPrescriptorId(String medicoPrescriptorId) { this.medicoPrescriptorId = medicoPrescriptorId; }

    public String getTipoPruebaId() { return tipoPruebaId; }
    public void setTipoPruebaId(String tipoPruebaId) { this.tipoPruebaId = tipoPruebaId; }

    public Prioridad getPrioridad() { return prioridad; }
    public void setPrioridad(Prioridad prioridad) { this.prioridad = prioridad; }

    public String getIndicacionClinica() { return indicacionClinica; }
    public void setIndicacionClinica(String indicacionClinica) { this.indicacionClinica = indicacionClinica; }

    public String getJustificacionDiagnostica() { return justificacionDiagnostica; }
    public void setJustificacionDiagnostica(String justificacionDiagnostica) { this.justificacionDiagnostica = justificacionDiagnostica; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud estado) { this.estado = estado; }

    public boolean isEsSeguimientoPeriodico() { return esSeguimientoPeriodico; }
    public void setEsSeguimientoPeriodico(boolean esSeguimientoPeriodico) { this.esSeguimientoPeriodico = esSeguimientoPeriodico; }

    public Integer getPeriodicidadMeses() { return periodicidadMeses; }
    public void setPeriodicidadMeses(Integer periodicidadMeses) { this.periodicidadMeses = periodicidadMeses; }
}

