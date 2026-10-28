package es.uclm.esi.iso2.sescam.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO para la firma digital de consentimientos informados y cuestionario de seguridad previo (RF-22 / CU-22).
 */
public class ConsentimientoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String pacienteId;
    private String tipoPruebaId;
    private LocalDateTime fechaFirma;
    private boolean firmado;
    private boolean checklistSeguridadCompletado;
    private boolean alergiaContrasteYodado;
    private boolean portaMarcapasosOImplanteMetalico;
    private double valorFiltradoGlomerular; // Insuficiencia renal para contraste
    private String firmaDigitalHash;

    public ConsentimientoDTO() {}

    public ConsentimientoDTO(String id, String pacienteId, String tipoPruebaId, LocalDateTime fechaFirma,
                             boolean firmado, boolean checklistSeguridadCompletado, boolean alergiaContrasteYodado,
                             boolean portaMarcapasosOImplanteMetalico, double valorFiltradoGlomerular,
                             String firmaDigitalHash) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.tipoPruebaId = tipoPruebaId;
        this.fechaFirma = fechaFirma;
        this.firmado = firmado;
        this.checklistSeguridadCompletado = checklistSeguridadCompletado;
        this.alergiaContrasteYodado = alergiaContrasteYodado;
        this.portaMarcapasosOImplanteMetalico = portaMarcapasosOImplanteMetalico;
        this.valorFiltradoGlomerular = valorFiltradoGlomerular;
        this.firmaDigitalHash = firmaDigitalHash;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }

    public String getTipoPruebaId() { return tipoPruebaId; }
    public void setTipoPruebaId(String tipoPruebaId) { this.tipoPruebaId = tipoPruebaId; }

    public LocalDateTime getFechaFirma() { return fechaFirma; }
    public void setFechaFirma(LocalDateTime fechaFirma) { this.fechaFirma = fechaFirma; }

    public boolean isFirmado() { return firmado; }
    public void setFirmado(boolean firmado) { this.firmado = firmado; }

    public boolean isChecklistSeguridadCompletado() { return checklistSeguridadCompletado; }
    public void setChecklistSeguridadCompletado(boolean checklistSeguridadCompletado) { this.checklistSeguridadCompletado = checklistSeguridadCompletado; }

    public boolean isAlergiaContrasteYodado() { return alergiaContrasteYodado; }
    public void setAlergiaContrasteYodado(boolean alergiaContrasteYodado) { this.alergiaContrasteYodado = alergiaContrasteYodado; }

    public boolean isPortaMarcapasosOImplanteMetalico() { return portaMarcapasosOImplanteMetalico; }
    public void setPortaMarcapasosOImplanteMetalico(boolean portaMarcapasosOImplanteMetalico) { this.portaMarcapasosOImplanteMetalico = portaMarcapasosOImplanteMetalico; }

    public double getValorFiltradoGlomerular() { return valorFiltradoGlomerular; }
    public void setValorFiltradoGlomerular(double valorFiltradoGlomerular) { this.valorFiltradoGlomerular = valorFiltradoGlomerular; }

    public String getFirmaDigitalHash() { return firmaDigitalHash; }
    public void setFirmaDigitalHash(String firmaDigitalHash) { this.firmaDigitalHash = firmaDigitalHash; }
}

