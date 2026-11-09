package es.uclm.esi.iso2.sescam.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO que representa una entrada en la lista de espera del sistema SESCAM.
 */
public class ListaEsperaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String pacienteId;
    private String tipoPruebaId;
    private LocalDateTime fechaInscripcion;
    private String estado; // Ej: "PENDIENTE", "ASIGNADA", "CANCELADA"

    public ListaEsperaDTO() {}

    public ListaEsperaDTO(String id, String pacienteId, String tipoPruebaId, LocalDateTime fechaInscripcion, String estado) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.tipoPruebaId = tipoPruebaId;
        this.fechaInscripcion = fechaInscripcion;
        this.estado = estado;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }

    public String getTipoPruebaId() { return tipoPruebaId; }
    public void setTipoPruebaId(String tipoPruebaId) { this.tipoPruebaId = tipoPruebaId; }

    public LocalDateTime getFechaInscripcion() { return fechaInscripcion; }
    public void setFechaInscripcion(LocalDateTime fechaInscripcion) { this.fechaInscripcion = fechaInscripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}