package es.uclm.esi.iso2.sescam.informes.dominio;

import es.uclm.esi.iso2.sescam.common.dto.RegistroRealizacionDTO;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa la ejecución material y técnica de una prueba radiológica (CU-10 / RF-10).
 */
public class RegistroRealizacion implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String citaId;
    private String pacienteId;
    private String tipoPruebaId;
    private String studyInstanceUID;
    private LocalDateTime fechaHoraEjecucion;
    private String medicoRadiologoId;
    private boolean contrasteAdministrado;
    private String tipoContraste;
    private double dosisRadiacionMgy;
    private String incidenciasTecnicasClinicas;
    private boolean consentimientoPrevioValidado;
    private String salaTecnicaId;

    public RegistroRealizacion() {
    }

    public RegistroRealizacion(String id, String citaId, String studyInstanceUID,
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

    public static RegistroRealizacion desdeDTO(RegistroRealizacionDTO dto) {
        if (dto == null) {
            return null;
        }
        RegistroRealizacion r = new RegistroRealizacion();
        r.setId(dto.getId());
        r.setCitaId(dto.getCitaId());
        r.setStudyInstanceUID(dto.getStudyInstanceUID());
        r.setFechaHoraEjecucion(dto.getFechaHoraEjecucion());
        r.setMedicoRadiologoId(dto.getMedicoRadiologoId());
        r.setContrasteAdministrado(dto.isContrasteAdministrado());
        r.setDosisRadiacionMgy(dto.getDosisRadiacionMgy());
        r.setIncidenciasTecnicasClinicas(dto.getIncidenciasTecnicasClinicas());
        r.setConsentimientoPrevioValidado(dto.isConsentimientoPrevioValidado());
        return r;
    }

    public RegistroRealizacionDTO aDTO() {
        RegistroRealizacionDTO dto = new RegistroRealizacionDTO();
        dto.setId(this.id);
        dto.setCitaId(this.citaId);
        dto.setStudyInstanceUID(this.studyInstanceUID);
        dto.setFechaHoraEjecucion(this.fechaHoraEjecucion);
        dto.setMedicoRadiologoId(this.medicoRadiologoId);
        dto.setContrasteAdministrado(this.contrasteAdministrado);
        dto.setDosisRadiacionMgy(this.dosisRadiacionMgy);
        dto.setIncidenciasTecnicasClinicas(this.incidenciasTecnicasClinicas);
        dto.setConsentimientoPrevioValidado(this.consentimientoPrevioValidado);
        return dto;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCitaId() { return citaId; }
    public void setCitaId(String citaId) { this.citaId = citaId; }

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }

    public String getTipoPruebaId() { return tipoPruebaId; }
    public void setTipoPruebaId(String tipoPruebaId) { this.tipoPruebaId = tipoPruebaId; }

    public String getStudyInstanceUID() { return studyInstanceUID; }
    public void setStudyInstanceUID(String studyInstanceUID) { this.studyInstanceUID = studyInstanceUID; }

    public LocalDateTime getFechaHoraEjecucion() { return fechaHoraEjecucion; }
    public void setFechaHoraEjecucion(LocalDateTime fechaHoraEjecucion) { this.fechaHoraEjecucion = fechaHoraEjecucion; }

    public String getMedicoRadiologoId() { return medicoRadiologoId; }
    public void setMedicoRadiologoId(String medicoRadiologoId) { this.medicoRadiologoId = medicoRadiologoId; }

    public boolean isContrasteAdministrado() { return contrasteAdministrado; }
    public void setContrasteAdministrado(boolean contrasteAdministrado) { this.contrasteAdministrado = contrasteAdministrado; }

    public String getTipoContraste() { return tipoContraste; }
    public void setTipoContraste(String tipoContraste) { this.tipoContraste = tipoContraste; }

    public double getDosisRadiacionMgy() { return dosisRadiacionMgy; }
    public void setDosisRadiacionMgy(double dosisRadiacionMgy) { this.dosisRadiacionMgy = dosisRadiacionMgy; }

    public String getIncidenciasTecnicasClinicas() { return incidenciasTecnicasClinicas; }
    public void setIncidenciasTecnicasClinicas(String incidenciasTecnicasClinicas) { this.incidenciasTecnicasClinicas = incidenciasTecnicasClinicas; }

    public boolean isConsentimientoPrevioValidado() { return consentimientoPrevioValidado; }
    public void setConsentimientoPrevioValidado(boolean consentimientoPrevioValidado) { this.consentimientoPrevioValidado = consentimientoPrevioValidado; }

    public String getSalaTecnicaId() { return salaTecnicaId; }
    public void setSalaTecnicaId(String salaTecnicaId) { this.salaTecnicaId = salaTecnicaId; }
}

