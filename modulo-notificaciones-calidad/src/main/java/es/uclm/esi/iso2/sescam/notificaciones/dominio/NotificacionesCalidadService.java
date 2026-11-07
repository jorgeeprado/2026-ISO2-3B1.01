package es.uclm.esi.iso2.sescam.notificaciones.dominio;

import es.uclm.esi.iso2.sescam.common.dto.IncidenciaDTO;
import es.uclm.esi.iso2.sescam.common.dto.NotificacionDTO;
import es.uclm.esi.iso2.sescam.common.enums.TipoNotificacion;
import es.uclm.esi.iso2.sescam.common.interfaces.INotificacionesCalidadService;
import es.uclm.esi.iso2.sescam.notificaciones.persistencia.INotificacionRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class NotificacionesCalidadService implements INotificacionesCalidadService {

    private final INotificacionRepository repository;
    private final List<ICanalNotificacion> canales;
    private final Clock clock;

    public NotificacionesCalidadService(
            INotificacionRepository repository,
            List<ICanalNotificacion> canales,
            Clock clock) {
        this.repository = Objects.requireNonNull(repository, "El repositorio no puede ser nulo");
        this.canales = List.copyOf(Objects.requireNonNull(canales, "Los canales no pueden ser nulos"));
        if (this.canales.isEmpty()) {
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

    @Override
    public NotificacionDTO emitirAlertaHallazgoCritico(
            String medicoPrescriptorId, String informeId, String detalleAlerta) {
        throw new UnsupportedOperationException("CU-24 está planificado para una iteración posterior.");
    }

    @Override
    public List<NotificacionDTO> consultarNotificaciones(String usuarioId) {
        validarIdentificador(usuarioId, "usuarioId");
        return repository.buscarPorDestinatario(usuarioId).stream()
                .map(Notificacion::getDto)
                .toList();
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
        NotificacionDTO dto = new NotificacionDTO(
                UUID.randomUUID().toString(),
                destinatarioId,
                tipo,
                titulo,
                mensaje,
                LocalDateTime.now(clock),
                false,
                referenciaId);
        repository.guardar(new Notificacion(dto, EstadoEnvio.PENDIENTE, 0, 0));

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

    private static void validarIdentificador(String valor, String nombre) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(nombre + " es obligatorio");
        }
    }

    private static NotificacionDTO copiar(NotificacionDTO origen) {
        return new NotificacionDTO(
                origen.getId(),
                origen.getDestinatarioId(),
                origen.getTipo(),
                origen.getTitulo(),
                origen.getMensaje(),
                origen.getFechaEmision(),
                origen.isLeida(),
                origen.getReferenciaEntidadId());
    }
}
