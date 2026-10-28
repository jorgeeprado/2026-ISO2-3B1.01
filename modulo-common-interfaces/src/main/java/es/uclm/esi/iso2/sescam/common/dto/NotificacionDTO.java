package es.uclm.esi.iso2.sescam.common.dto;

import es.uclm.esi.iso2.sescam.common.enums.TipoNotificacion;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO para la emisión y consulta de avisos y recordatorios multicanal (RF-02 / CU-02).
 */
public class NotificacionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String destinatarioId;
    private TipoNotificacion tipo;
    private String titulo;
    private String mensaje;
    private LocalDateTime fechaEmision;
    private boolean leida;
    private String referenciaEntidadId; // id de cita, informe, etc.

    public NotificacionDTO() {}

    public NotificacionDTO(String id, String destinatarioId, TipoNotificacion tipo, String titulo,
                           String mensaje, LocalDateTime fechaEmision, boolean leida, String referenciaEntidadId) {
        this.id = id;
        this.destinatarioId = destinatarioId;
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.fechaEmision = fechaEmision;
        this.leida = leida;
        this.referenciaEntidadId = referenciaEntidadId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDestinatarioId() { return destinatarioId; }
    public void setDestinatarioId(String destinatarioId) { this.destinatarioId = destinatarioId; }

    public TipoNotificacion getTipo() { return tipo; }
    public void setTipo(TipoNotificacion tipo) { this.tipo = tipo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }

    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }

    public String getReferenciaEntidadId() { return referenciaEntidadId; }
    public void setReferenciaEntidadId(String referenciaEntidadId) { this.referenciaEntidadId = referenciaEntidadId; }
}

