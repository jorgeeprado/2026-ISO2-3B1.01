package es.uclm.esi.iso2.sescam.common.dto;

import es.uclm.esi.iso2.sescam.common.enums.EstadoInforme;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO para la elaboración, firma digital y consulta de informes radiológicos (RF-09 / CU-09).
 */
public class InformeRadiologicoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String citaId;
    private String pacienteId;
    private String medicoPrescriptorId;
    private String medicoRadiologoId;
    private String studyInstanceUID;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaFirma;
    private String hallazgos;
    private String conclusionDiagnostica;
    private boolean hallazgoCritico; // Dispara alerta prioritaria según RF-24 / CU-24
    private EstadoInforme estado;
    private String firmaDigitalHash; // Hash de la firma electrónica

    public InformeRadiologicoDTO() {}

    public InformeRadiologicoDTO(String id, String citaId, String pacienteId, String medicoPrescriptorId,
                                 String medicoRadiologoId, String studyInstanceUID, LocalDateTime fechaCreacion,
                                 LocalDateTime fechaFirma, String hallazgos, String conclusionDiagnostica,
                                 boolean hallazgoCritico, EstadoInforme estado, String firmaDigitalHash) {
        this.id = id;
        this.citaId = citaId;
        this.pacienteId = pacienteId;
        this.medicoPrescriptorId = medicoPrescriptorId;
        this.medicoRadiologoId = medicoRadiologoId;
        this.studyInstanceUID = studyInstanceUID;
        this.fechaCreacion = fechaCreacion;
        this.fechaFirma = fechaFirma;
        this.hallazgos = hallazgos;
        this.conclusionDiagnostica = conclusionDiagnostica;
        this.hallazgoCritico = hallazgoCritico;
        this.estado = estado;
        this.firmaDigitalHash = firmaDigitalHash;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCitaId() { return citaId; }
    public void setCitaId(String citaId) { this.citaId = citaId; }

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }

    public String getMedicoPrescriptorId() { return medicoPrescriptorId; }
    public void setMedicoPrescriptorId(String medicoPrescriptorId) { this.medicoPrescriptorId = medicoPrescriptorId; }

    public String getMedicoRadiologoId() { return medicoRadiologoId; }
    public void setMedicoRadiologoId(String medicoRadiologoId) { this.medicoRadiologoId = medicoRadiologoId; }

    public String getStudyInstanceUID() { return studyInstanceUID; }
    public void setStudyInstanceUID(String studyInstanceUID) { this.studyInstanceUID = studyInstanceUID; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaFirma() { return fechaFirma; }
    public void setFechaFirma(LocalDateTime fechaFirma) { this.fechaFirma = fechaFirma; }

    public String getHallazgos() { return hallazgos; }
    public void setHallazgos(String hallazgos) { this.hallazgos = hallazgos; }

    public String getConclusionDiagnostica() { return conclusionDiagnostica; }
    public void setConclusionDiagnostica(String conclusionDiagnostica) { this.conclusionDiagnostica = conclusionDiagnostica; }

    public boolean isHallazgoCritico() { return hallazgoCritico; }
    public void setHallazgoCritico(boolean hallazgoCritico) { this.hallazgoCritico = hallazgoCritico; }

    public EstadoInforme getEstado() { return estado; }
    public void setEstado(EstadoInforme estado) { this.estado = estado; }

    public String getFirmaDigitalHash() { return firmaDigitalHash; }
    public void setFirmaDigitalHash(String firmaDigitalHash) { this.firmaDigitalHash = firmaDigitalHash; }
}
