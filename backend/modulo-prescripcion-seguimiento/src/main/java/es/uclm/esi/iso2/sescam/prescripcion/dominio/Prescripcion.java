package es.uclm.esi.iso2.sescam.prescripcion.dominio;

import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoSolicitud;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de dominio que representa una prescripción de prueba diagnóstica.
 */
public class Prescripcion {

    private final String id;
    private final String pacienteId;
    private final String medicoPrescriptorId;
    private final String tipoPruebaId;
    private final Prioridad prioridad;
    private final String indicacionClinica;
    private final String justificacionDiagnostica;
    private final LocalDateTime fechaSolicitud;
    private final EstadoSolicitud estado;
    private final boolean seguimientoPeriodico;
    private final Integer periodicidadMeses;

    public Prescripcion(String id, String pacienteId, String medicoPrescriptorId, String tipoPruebaId,
                        Prioridad prioridad, String indicacionClinica, String justificacionDiagnostica,
                        LocalDateTime fechaSolicitud, EstadoSolicitud estado, boolean seguimientoPeriodico,
                        Integer periodicidadMeses) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.medicoPrescriptorId = medicoPrescriptorId;
        this.tipoPruebaId = tipoPruebaId;
        this.prioridad = prioridad;
        this.indicacionClinica = indicacionClinica;
        this.justificacionDiagnostica = justificacionDiagnostica;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = estado;
        this.seguimientoPeriodico = seguimientoPeriodico;
        this.periodicidadMeses = periodicidadMeses;
    }

    public static Prescripcion desdeDTO(SolicitudPruebaDTO solicitud) {
        Objects.requireNonNull(solicitud, "La solicitud no puede ser nula");
        return new Prescripcion(
                solicitud.getId(),
                solicitud.getPacienteId(),
                solicitud.getMedicoPrescriptorId(),
                solicitud.getTipoPruebaId(),
                solicitud.getPrioridad(),
                solicitud.getIndicacionClinica(),
                solicitud.getJustificacionDiagnostica(),
                solicitud.getFechaSolicitud(),
                solicitud.getEstado(),
                solicitud.isEsSeguimientoPeriodico(),
                solicitud.getPeriodicidadMeses());
    }

    public SolicitudPruebaDTO aDTO() {
        return new SolicitudPruebaDTO(id, pacienteId, medicoPrescriptorId, tipoPruebaId, prioridad,
                indicacionClinica, justificacionDiagnostica, fechaSolicitud, estado,
                seguimientoPeriodico, periodicidadMeses);
    }

    public String getId() { return id; }
    public String getPacienteId() { return pacienteId; }
    public String getMedicoPrescriptorId() { return medicoPrescriptorId; }
    public String getTipoPruebaId() { return tipoPruebaId; }
    public Prioridad getPrioridad() { return prioridad; }
    public String getIndicacionClinica() { return indicacionClinica; }
    public String getJustificacionDiagnostica() { return justificacionDiagnostica; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public EstadoSolicitud getEstado() { return estado; }
    public boolean isSeguimientoPeriodico() { return seguimientoPeriodico; }
    public Integer getPeriodicidadMeses() { return periodicidadMeses; }
}
