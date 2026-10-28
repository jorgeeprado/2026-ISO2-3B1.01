package es.uclm.esi.iso2.sescam.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO que documenta la ejecución técnica y clínica de una prueba radiológica (RF-10 / CU-10).
 */
public class RegistroRealizacionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String citaId;
    private String studyInstanceUID; // Identificador universal del estudio DICOM
    private LocalDateTime fechaHoraEjecucion;
    private String medicoRadiologoId;
    private boolean contrasteAdministrado;
    private double dosisRadiacionMgy; // Dosis de radiación registrada
    private String incidenciasTecnicasClinicas;
    private boolean consentimientoPrevioValidado; // Bloqueo si false conforme a RN-04

    public RegistroRealizacionDTO() {}

    public RegistroRealizacionDTO(String id, String citaId, String studyInstanceUID,
                                  LocalDateTime fechaHoraEjecucion, String medicoRadiologoId,
                                  boolean contrasteAdministrado, double dosisRadiacionMgy,
                                  String incidenciasTecnicasClinicas, boolean consentimientoPrevioValidado) {
        this.id = id;
        this.citaId = citaId;
        this.studyInstanceUID = studyInstanceUID;
        this.fechaHoraEjecucion = fechaHoraEjecucion;
        this.medicoRadiologoId = medicoRadiologoId;
        this.contrasteAdministrado = contrasteAdministrado;
        this.dosisRadiacionMgy = dosisRadiacionMgy;
        this.incidenciasTecnicasClinicas = incidenciasTecnicasClinicas;
        this.consentimientoPrevioValidado = consentimientoPrevioValidado;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCitaId() { return citaId; }
    public void setCitaId(String citaId) { this.citaId = citaId; }

    public String getStudyInstanceUID() { return studyInstanceUID; }
    public void setStudyInstanceUID(String studyInstanceUID) { this.studyInstanceUID = studyInstanceUID; }

    public LocalDateTime getFechaHoraEjecucion() { return fechaHoraEjecucion; }
    public void setFechaHoraEjecucion(LocalDateTime fechaHoraEjecucion) { this.fechaHoraEjecucion = fechaHoraEjecucion; }

    public String getMedicoRadiologoId() { return medicoRadiologoId; }
    public void setMedicoRadiologoId(String medicoRadiologoId) { this.medicoRadiologoId = medicoRadiologoId; }

    public boolean isContrasteAdministrado() { return contrasteAdministrado; }
    public void setContrasteAdministrado(boolean contrasteAdministrado) { this.contrasteAdministrado = contrasteAdministrado; }

    public double getDosisRadiacionMgy() { return dosisRadiacionMgy; }
    public void setDosisRadiacionMgy(double dosisRadiacionMgy) { this.dosisRadiacionMgy = dosisRadiacionMgy; }

    public String getIncidenciasTecnicasClinicas() { return incidenciasTecnicasClinicas; }
    public void setIncidenciasTecnicasClinicas(String incidenciasTecnicasClinicas) { this.incidenciasTecnicasClinicas = incidenciasTecnicasClinicas; }

    public boolean isConsentimientoPrevioValidado() { return consentimientoPrevioValidado; }
    public void setConsentimientoPrevioValidado(boolean consentimientoPrevioValidado) { this.consentimientoPrevioValidado = consentimientoPrevioValidado; }
}

