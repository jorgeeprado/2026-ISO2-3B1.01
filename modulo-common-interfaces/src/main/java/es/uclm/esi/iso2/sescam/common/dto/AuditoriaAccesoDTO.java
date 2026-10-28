package es.uclm.esi.iso2.sescam.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO para el registro síncrono e inmutable de auditoría de accesos a datos clínicos y estudios DICOM (RN-07 / RNF-06 / CU-19).
 */
public class AuditoriaAccesoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private LocalDateTime timestamp;
    private String usuarioId;
    private String rolUsuario;
    private String pacienteIdAfectado;
    private String accion; // LECTURA, CREACION, MODIFICACION, FIRMA, EXPORTACION
    private String recursoAccedido; // HISTORIA_CLINICA, INFORME_DICOM, SERIE_IMAGENES
    private String ipOrigen;

    public AuditoriaAccesoDTO() {}

    public AuditoriaAccesoDTO(String id, LocalDateTime timestamp, String usuarioId, String rolUsuario,
                              String pacienteIdAfectado, String accion, String recursoAccedido, String ipOrigen) {
        this.id = id;
        this.timestamp = timestamp;
        this.usuarioId = usuarioId;
        this.rolUsuario = rolUsuario;
        this.pacienteIdAfectado = pacienteIdAfectado;
        this.accion = accion;
        this.recursoAccedido = recursoAccedido;
        this.ipOrigen = ipOrigen;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getRolUsuario() { return rolUsuario; }
    public void setRolUsuario(String rolUsuario) { this.rolUsuario = rolUsuario; }

    public String getPacienteIdAfectado() { return pacienteIdAfectado; }
    public void setPacienteIdAfectado(String pacienteIdAfectado) { this.pacienteIdAfectado = pacienteIdAfectado; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getRecursoAccedido() { return recursoAccedido; }
    public void setRecursoAccedido(String recursoAccedido) { this.recursoAccedido = recursoAccedido; }

    public String getIpOrigen() { return ipOrigen; }
    public void setIpOrigen(String ipOrigen) { this.ipOrigen = ipOrigen; }
}
