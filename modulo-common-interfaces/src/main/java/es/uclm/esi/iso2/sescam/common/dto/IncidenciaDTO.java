package es.uclm.esi.iso2.sescam.common.dto;

import es.uclm.esi.iso2.sescam.common.enums.TipoIncidencia;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO para la comunicación de incidencias y encuestas de satisfacción de pacientes (RF-04 / CU-04).
 */
public class IncidenciaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String pacienteId;
    private String citaId;
    private TipoIncidencia tipo;
    private String descripcion;
    private Integer puntuacionSatisfaccion; // 1 a 5 si es encuesta de calidad
    private LocalDateTime fechaRegistro;

    public IncidenciaDTO() {}

    public IncidenciaDTO(String id, String pacienteId, String citaId, TipoIncidencia tipo,
                         String descripcion, Integer puntuacionSatisfaccion, LocalDateTime fechaRegistro) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.citaId = citaId;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.puntuacionSatisfaccion = puntuacionSatisfaccion;
        this.fechaRegistro = fechaRegistro;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }

    public String getCitaId() { return citaId; }
    public void setCitaId(String citaId) { this.citaId = citaId; }

    public TipoIncidencia getTipo() { return tipo; }
    public void setTipo(TipoIncidencia tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getPuntuacionSatisfaccion() { return puntuacionSatisfaccion; }
    public void setPuntuacionSatisfaccion(Integer puntuacionSatisfaccion) { this.puntuacionSatisfaccion = puntuacionSatisfaccion; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}

