package es.uclm.esi.iso2.sescam.common.dto;

import es.uclm.esi.iso2.sescam.common.enums.ModalidadPrueba;
import java.io.Serializable;

/**
 * DTO que define un tipo de prueba del catálogo de radiodiagnóstico (RF-17).
 */
public class PruebaCatalogoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String codigo;
    private String nombre;
    private ModalidadPrueba modalidad;
    private int duracionEstimadaMinutos;
    private boolean requiereContraste;
    private boolean requiereConsentimientoInformado;
    private String instruccionesPreparacion;
    private boolean admiteSeguimientoPeriodico;
    private boolean activa;

    public PruebaCatalogoDTO() {}

    public PruebaCatalogoDTO(String id, String codigo, String nombre, ModalidadPrueba modalidad,
                             int duracionEstimadaMinutos, boolean requiereContraste,
                             boolean requiereConsentimientoInformado, String instruccionesPreparacion,
                             boolean admiteSeguimientoPeriodico, boolean activa) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.modalidad = modalidad;
        this.duracionEstimadaMinutos = duracionEstimadaMinutos;
        this.requiereContraste = requiereContraste;
        this.requiereConsentimientoInformado = requiereConsentimientoInformado;
        this.instruccionesPreparacion = instruccionesPreparacion;
        this.admiteSeguimientoPeriodico = admiteSeguimientoPeriodico;
        this.activa = activa;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public ModalidadPrueba getModalidad() { return modalidad; }
    public void setModalidad(ModalidadPrueba modalidad) { this.modalidad = modalidad; }

    public int getDuracionEstimadaMinutos() { return duracionEstimadaMinutos; }
    public void setDuracionEstimadaMinutos(int duracionEstimadaMinutos) { this.duracionEstimadaMinutos = duracionEstimadaMinutos; }

    public boolean isRequiereContraste() { return requiereContraste; }
    public void setRequiereContraste(boolean requiereContraste) { this.requiereContraste = requiereContraste; }

    public boolean isRequiereConsentimientoInformado() { return requiereConsentimientoInformado; }
    public void setRequiereConsentimientoInformado(boolean requiereConsentimientoInformado) { this.requiereConsentimientoInformado = requiereConsentimientoInformado; }

    public String getInstruccionesPreparacion() { return instruccionesPreparacion; }
    public void setInstruccionesPreparacion(String instruccionesPreparacion) { this.instruccionesPreparacion = instruccionesPreparacion; }

    public boolean isAdmiteSeguimientoPeriodico() { return admiteSeguimientoPeriodico; }
    public void setAdmiteSeguimientoPeriodico(boolean admiteSeguimientoPeriodico) { this.admiteSeguimientoPeriodico = admiteSeguimientoPeriodico; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}

