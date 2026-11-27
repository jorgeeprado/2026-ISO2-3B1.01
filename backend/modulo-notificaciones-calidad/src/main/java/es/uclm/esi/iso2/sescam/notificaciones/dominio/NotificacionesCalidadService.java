package es.uclm.esi.iso2.sescam.notificaciones.dominio;

import es.uclm.esi.iso2.sescam.common.dto.IncidenciaDTO;
import es.uclm.esi.iso2.sescam.common.dto.FiltroNotificacionDTO;
import es.uclm.esi.iso2.sescam.common.dto.NotificacionDTO;
import es.uclm.esi.iso2.sescam.common.enums.TipoNotificacion;
import es.uclm.esi.iso2.sescam.common.interfaces.INotificacionesCalidadService;
import es.uclm.esi.iso2.sescam.notificaciones.persistencia.INotificacionRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificacionesCalidadService implements INotificacionesCalidadService {

    /** Longitud máxima del detalle en una alerta de CU-24; deja margen en la columna de mensaje (4000). */
    static final int LONGITUD_MAXIMA_DETALLE = 2000;

    private final INotificacionRepository repository;
    private final List<ICanalNotificacion> canales;
    private final Clock clock;

    public NotificacionesCalidadService(
            INotificacionRepository repository,
            List<ICanalNotificacion> canales,
            Clock clock) {
        this(repository, canales, clock, false);
    }

    @Autowired
    public NotificacionesCalidadService(
            INotificacionRepository repository,
            ObjectProvider<ICanalNotificacion> canales,
            Clock clock) {
        this(repository, canales.orderedStream().toList(), clock, true);
    }

    private NotificacionesCalidadService(
            INotificacionRepository repository,
            List<ICanalNotificacion> canales,
            Clock clock,
            boolean permitirSinCanales) {
        this.repository = Objects.requireNonNull(repository, "El repositorio no puede ser nulo");
        this.canales = List.copyOf(Objects.requireNonNull(canales, "Los canales no pueden ser nulos"));
        if (this.canales.isEmpty() && !permitirSinCanales) {
            throw new IllegalArgumentException("Debe configurarse al menos un canal de notificación");
        }
        this.clock = Objects.requireNonNull(clock, "El reloj no puede ser nulo");
    }

    @Override
    public NotificacionDTO enviarRecordatorioCita(String citaId, String pacienteId) {
        validarIdentificador(citaId, "citaId");
        validarIdentificador(pacienteId, "pacienteId");
        return emitir(
                pacienteId,
                TipoNotificacion.RECORDATORIO_CITA,
                "Recordatorio de cita de radiodiagnóstico",
                "Tiene una cita de radiodiagnóstico próxima. Revise las indicaciones de preparación previa, "
                        + "incluidas las relativas al ayuno o al contraste que le haya facilitado el equipo sanitario.",
                citaId);
    }

    @Override
    public NotificacionDTO notificarInformeDisponible(String pacienteId, String informeId) {
        validarIdentificador(pacienteId, "pacienteId");
        validarIdentificador(informeId, "informeId");
        return emitir(
                pacienteId,
                TipoNotificacion.INFORME_DISPONIBLE,
                "Informe radiológico disponible",
                "Su informe radiológico está disponible para su consulta.",
                informeId);
    }

    /**
     * CU-24 / RF-24 / RN-06: emite de forma síncrona una alerta prioritaria multicanal al facultativo
     * prescriptor cuando el radiólogo marca un hallazgo crítico inesperado en un informe.
     *
     * <p>Se invoca como extensión ({@code <<extend>>}) de CU-09 cuando {@code informe.isHallazgoCritico()}.
     * La alerta se persiste antes del despacho y se intenta por todos los canales configurados; si alguno
     * falla se registra el estado ({@code PARCIAL} o {@code FALLIDA}) y se propaga
     * {@link NotificacionDispatchException} para que el emisor sepa que el aviso no llegó completo.</p>
     *
     * <p>Un detalle de más de {@value #LONGITUD_MAXIMA_DETALLE} caracteres se recorta en vez de rechazarse,
     * para no perder el aviso; el texto completo se consulta en el informe referenciado.</p>
     *
     * @throws IllegalArgumentException si falta algún dato
     * @throws IllegalStateException si no hay canales de notificación configurados
     * @throws NotificacionDispatchException si algún canal no entrega la alerta
     */
    @Override
    public NotificacionDTO emitirAlertaHallazgoCritico(
            String medicoPrescriptorId, String informeId, String detalleAlerta) {
        validarIdentificador(medicoPrescriptorId, "medicoPrescriptorId");
        validarIdentificador(informeId, "informeId");
        validarIdentificador(detalleAlerta, "detalleAlerta");
        String detalle = recortar(detalleAlerta.strip());
        return emitir(
                medicoPrescriptorId,
                TipoNotificacion.ALERTA_HALLAZGO_CRITICO,
                "ALERTA PRIORITARIA: hallazgo radiológico crítico",
                "Se ha detectado un hallazgo crítico inesperado en el informe radiológico " + informeId
                        + " de un paciente a su cargo. Detalle: " + detalle
                        + ". Revise el informe y actúe con carácter prioritario.",
                informeId);
    }

    @Override
    public List<NotificacionDTO> consultarNotificaciones(String usuarioId) {
        validarIdentificador(usuarioId, "usuarioId");
        return repository.buscarPorDestinatario(usuarioId).stream()
                .map(Notificacion::getDto)
                .toList();
    }

    @Override
    public List<NotificacionDTO> consultarNotificacionesPorUsuario(FiltroNotificacionDTO filtro) {
        validarFiltro(filtro);
        return repository.buscar(filtro).stream()
                .map(Notificacion::getDto)
                .toList();
    }

    @Override
    public NotificacionDTO marcarComoLeida(Long notificacionId) {
        if (notificacionId == null || notificacionId <= 0) {
            throw new IllegalArgumentException("El id numérico de notificación debe ser positivo");
        }
        return repository.marcarComoLeida(notificacionId).getDto();
    }

    @Override
    public IncidenciaDTO registrarIncidenciaOEncuesta(IncidenciaDTO incidencia) {
        throw new UnsupportedOperationException("CU-04 está planificado para una iteración posterior.");
    }

    private NotificacionDTO emitir(
            String destinatarioId,
            TipoNotificacion tipo,
            String titulo,
            String mensaje,
            String referenciaId) {
        if (canales.isEmpty()) {
            throw new IllegalStateException("No hay canales de notificación configurados");
        }
        NotificacionDTO dto = new NotificacionDTO(
                UUID.randomUUID().toString(),
                destinatarioId,
                tipo,
                titulo,
                mensaje,
                LocalDateTime.now(clock),
                false,
                referenciaId);
        Notificacion guardada = repository.guardar(new Notificacion(dto, EstadoEnvio.PENDIENTE, 0, 0));
        dto.setNotificacionId(guardada.getNotificacionId());

        int enviados = 0;
        int fallidos = 0;
        NotificacionDeliveryException primerError = null;
        for (ICanalNotificacion canal : canales) {
            try {
                canal.enviar(copiar(dto));
                enviados++;
            } catch (NotificacionDeliveryException error) {
                fallidos++;
                if (primerError == null) {
                    primerError = error;
                }
            }
        }

        EstadoEnvio estado = fallidos == 0
                ? EstadoEnvio.ENVIADA
                : enviados == 0 ? EstadoEnvio.FALLIDA : EstadoEnvio.PARCIAL;
        repository.guardar(new Notificacion(dto, estado, enviados, fallidos));
        if (primerError != null) {
            throw new NotificacionDispatchException(dto.getId(), estado, primerError);
        }
        return copiar(dto);
    }

    private static String recortar(String detalle) {
        if (detalle.length() <= LONGITUD_MAXIMA_DETALLE) {
            return detalle;
        }
        return detalle.substring(0, LONGITUD_MAXIMA_DETALLE - 1) + "…";
    }

    private static void validarIdentificador(String valor, String nombre) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(nombre + " es obligatorio");
        }
    }

    private static void validarFiltro(FiltroNotificacionDTO filtro) {
        Objects.requireNonNull(filtro, "El filtro no puede ser nulo");
        validarIdentificador(filtro.getUsuarioId(), "usuarioId");
        if (filtro.getFechaDesde() != null && filtro.getFechaHasta() != null
                && filtro.getFechaDesde().isAfter(filtro.getFechaHasta())) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta");
        }
    }

    private static NotificacionDTO copiar(NotificacionDTO origen) {
        return new NotificacionDTO(
                origen.getId(),
                origen.getNotificacionId(),
                origen.getDestinatarioId(),
                origen.getTipo(),
                origen.getTitulo(),
                origen.getMensaje(),
                origen.getFechaEmision(),
                origen.isLeida(),
                origen.getReferenciaEntidadId());
    }
}
