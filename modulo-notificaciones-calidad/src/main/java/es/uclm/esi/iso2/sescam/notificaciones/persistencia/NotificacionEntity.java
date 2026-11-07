package es.uclm.esi.iso2.sescam.notificaciones.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.NotificacionDTO;
import es.uclm.esi.iso2.sescam.common.enums.TipoNotificacion;
import es.uclm.esi.iso2.sescam.notificaciones.dominio.EstadoEnvio;
import es.uclm.esi.iso2.sescam.notificaciones.dominio.Notificacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones", indexes = {
        @Index(name = "idx_notificacion_destinatario_fecha", columnList = "destinatario_id, fecha_emision")
})
public class NotificacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identificador_publico", nullable = false, unique = true)
    private String identificadorPublico;

    @Column(name = "destinatario_id", nullable = false)
    private String destinatarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacion tipo;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 4000)
    private String mensaje;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @Column(nullable = false)
    private boolean leida;

    @Column(name = "referencia_entidad_id")
    private String referenciaEntidadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_envio", nullable = false)
    private EstadoEnvio estadoEnvio;

    @Column(name = "canales_enviados", nullable = false)
    private int canalesEnviados;

    @Column(name = "canales_fallidos", nullable = false)
    private int canalesFallidos;

    protected NotificacionEntity() {}

    public void actualizarDesde(Notificacion notificacion) {
        NotificacionDTO dto = notificacion.getDto();
        identificadorPublico = dto.getId();
        destinatarioId = dto.getDestinatarioId();
        tipo = dto.getTipo();
        titulo = dto.getTitulo();
        mensaje = dto.getMensaje();
        fechaEmision = dto.getFechaEmision();
        leida = dto.isLeida();
        referenciaEntidadId = dto.getReferenciaEntidadId();
        estadoEnvio = notificacion.getEstadoEnvio();
        canalesEnviados = notificacion.getCanalesEnviados();
        canalesFallidos = notificacion.getCanalesFallidos();
    }

    public Notificacion aDominio() {
        NotificacionDTO dto = new NotificacionDTO(
                identificadorPublico,
                id,
                destinatarioId,
                tipo,
                titulo,
                mensaje,
                fechaEmision,
                leida,
                referenciaEntidadId);
        return new Notificacion(id, dto, estadoEnvio, canalesEnviados, canalesFallidos);
    }

}
