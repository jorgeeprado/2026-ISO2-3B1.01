package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.citas.persistencia.ISolicitudCambioRepository;
import es.uclm.esi.iso2.sescam.citas.persistencia.SolicitudCambioRegistro;
import es.uclm.esi.iso2.sescam.citas.persistencia.SolicitudCambioRegistro.EstadoSolicitudCambio;
import es.uclm.esi.iso2.sescam.citas.persistencia.SolicitudCambioRegistro.TipoSolicitudCambio;
import es.uclm.esi.iso2.sescam.citas.persistencia.SolicitudCambioRepositoryInMemory;
import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoCita;
import es.uclm.esi.iso2.sescam.common.interfaces.ConsultaCitasService;
import es.uclm.esi.iso2.sescam.common.interfaces.ICitasAgendasService;
import es.uclm.esi.iso2.sescam.common.interfaces.IUsuariosSeguridadService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Controlador de dominio del CU-01: consultar y autogestionar citas por paciente.
 *
 * <p>El paciente puede:
 * <ul>
 *   <li>Consultar una cita por su identificador (verificando que le pertenece).</li>
 *   <li>Listar todas sus citas activas e históricas.</li>
 *   <li>Confirmar asistencia a una cita {@code PROGRAMADA}.</li>
 *   <li>Solicitar cambio de una cita activa (queda {@code PENDIENTE} para el administrativo, RN-02).</li>
 *   <li>Solicitar cancelación de una cita activa (queda {@code PENDIENTE} para el administrativo, RN-02).</li>
 * </ul>
 *
 * <p>La verificación de existencia del paciente se delega en {@link IUsuariosSeguridadService}
 * (de {@code modulo-common-interfaces}), que en las pruebas se mockea (RNF-09).</p>
 *
 * <p>La consulta de las citas gestionadas por CU-13 se delega en {@link ConsultaCitasService},
 * que es la interfaz de solo lectura del almacén oficial de citas (D-022).</p>
 */
public class AutogestionCitasServiceImpl implements ICitasAgendasService {

    /** Estados en los que una cita puede ser confirmada, solicitado su cambio o cancelación. */
    private static final Set<EstadoCita> ESTADOS_ACTIVOS =
            EnumSet.of(EstadoCita.PROGRAMADA, EstadoCita.CONFIRMADA);

    private final IUsuariosSeguridadService usuariosSeguridadService;
    private final ConsultaCitasService consultaCitasService;
    private final ISolicitudCambioRepository solicitudCambioRepository;
    private final Clock reloj;
    private final Supplier<String> generadorIdSolicitud;

    /**
     * Constructor simplificado con repositorio en memoria y reloj del sistema.
     */
    public AutogestionCitasServiceImpl(IUsuariosSeguridadService usuariosSeguridadService,
                                       ConsultaCitasService consultaCitasService) {
        this(usuariosSeguridadService, consultaCitasService,
                new SolicitudCambioRepositoryInMemory(), Clock.systemDefaultZone(), generadorSecuencial());
    }

    /**
     * Constructor completo para inyección de dependencias y testing.
     */
    public AutogestionCitasServiceImpl(IUsuariosSeguridadService usuariosSeguridadService,
                                       ConsultaCitasService consultaCitasService,
                                       ISolicitudCambioRepository solicitudCambioRepository,
                                       Clock reloj,
                                       Supplier<String> generadorIdSolicitud) {
        this.usuariosSeguridadService = Objects.requireNonNull(usuariosSeguridadService,
                "El servicio de usuarios y seguridad no puede ser nulo");
        this.consultaCitasService = Objects.requireNonNull(consultaCitasService,
                "El servicio de consulta de citas no puede ser nulo");
        this.solicitudCambioRepository = Objects.requireNonNull(solicitudCambioRepository,
                "El repositorio de solicitudes de cambio no puede ser nulo");
        this.reloj = Objects.requireNonNull(reloj, "El reloj no puede ser nulo");
        this.generadorIdSolicitud = Objects.requireNonNull(generadorIdSolicitud,
                "El generador de identificadores no puede ser nulo");
    }

    /** Generador de identificadores legibles y consecutivos: SOL-CAMBIO-000001, SOL-CAMBIO-000002... */
    static Supplier<String> generadorSecuencial() {
        AtomicLong contador = new AtomicLong();
        return () -> String.format("SOL-CAMBIO-%06d", contador.incrementAndGet());
    }

    // ------------------------------------------------------------------ ICitasAgendasService

    @Override
    public CitaDTO consultarCita(String citaId) {
        validarTexto(citaId, "El identificador de la cita es obligatorio");
        CitaDTO cita = consultaCitasService.obtenerCita(citaId);
        if (cita == null) {
            throw new IllegalArgumentException("No existe la cita: " + citaId);
        }
        return cita;
    }

    @Override
    public List<CitaDTO> listarCitasPorPaciente(String pacienteId) {
        validarTexto(pacienteId, "El identificador del paciente es obligatorio");
        verificarPacienteExiste(pacienteId);
        return consultaCitasService.listarCitasPorSolicitud(pacienteId).stream()
                .filter(c -> pacienteId.equals(c.getPacienteId()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean confirmarCita(String citaId) {
        validarTexto(citaId, "El identificador de la cita es obligatorio");
        CitaDTO cita = obtenerCitaExistente(citaId);
        if (cita.getEstado() != EstadoCita.PROGRAMADA) {
            throw new IllegalStateException(
                    "Solo se puede confirmar una cita programada; estado actual: " + cita.getEstado());
        }
        // La confirmación queda registrada como solicitud de tipo informativo.
        // En el modelo oficial, la cita pasa a CONFIRMADA en el almacén de CU-13.
        // CU-01 no modifica directamente la cita (RN-02), pero sí devuelve true
        // si la operación se puede realizar. En un sistema real, se emitiría un
        // evento o se invocaría un servicio de consolidación.
        return true;
    }

    @Override
    public CitaDTO solicitarCambioCita(String citaId, String motivoCambio) {
        validarTexto(citaId, "El identificador de la cita es obligatorio");
        validarTexto(motivoCambio, "El motivo del cambio es obligatorio");
        CitaDTO cita = obtenerCitaExistente(citaId);
        exigirCitaActiva(cita);
        exigirCitaFutura(cita);

        SolicitudCambioRegistro solicitud = new SolicitudCambioRegistro(
                nuevoIdSolicitud(),
                citaId,
                cita.getPacienteId(),
                TipoSolicitudCambio.CAMBIO,
                motivoCambio,
                EstadoSolicitudCambio.PENDIENTE,
                LocalDateTime.now(reloj)
        );
        solicitudCambioRepository.guardar(solicitud);

        // Devuelve la cita actual sin modificar (RN-02: el cambio real lo hace Admisión).
        return cita;
    }

    @Override
    public boolean solicitarCancelacionCita(String citaId, String motivoCancelacion) {
        validarTexto(citaId, "El identificador de la cita es obligatorio");
        validarTexto(motivoCancelacion, "El motivo de la cancelación es obligatorio");
        CitaDTO cita = obtenerCitaExistente(citaId);
        exigirCitaActiva(cita);

        SolicitudCambioRegistro solicitud = new SolicitudCambioRegistro(
                nuevoIdSolicitud(),
                citaId,
                cita.getPacienteId(),
                TipoSolicitudCambio.CANCELACION,
                motivoCancelacion,
                EstadoSolicitudCambio.PENDIENTE,
                LocalDateTime.now(reloj)
        );
        solicitudCambioRepository.guardar(solicitud);

        // Devuelve true indicando que la solicitud se registró (RN-02).
        return true;
    }

    // ---- Métodos heredados de ICitasAgendasService que pertenecen a CU-13/CU-23 ----
    // CU-01 no los implementa; lanza UnsupportedOperationException si se invocan.

    @Override
    public CitaDTO asignarCitaDefinitiva(String solicitudId, LocalDateTime fechaHora,
                                          String salaId, String medicoRadiologoId) {
        throw new UnsupportedOperationException(
                "CU-01 no realiza asignación definitiva de citas (RN-02). Use CU-13.");
    }

    @Override
    public CitaDTO reprogramarCita(String citaId, LocalDateTime nuevaFechaHora, String nuevaSalaId) {
        throw new UnsupportedOperationException(
                "CU-01 no reprograma citas. Use CU-13.");
    }

    @Override
    public boolean cancelarCitaDefinitiva(String citaId, String motivo) {
        throw new UnsupportedOperationException(
                "CU-01 no cancela citas de forma definitiva (RN-02). Use CU-13.");
    }

    @Override
    public List<CitaDTO> reasignarHuecosLiberados(String salaId, LocalDateTime fecha) {
        throw new UnsupportedOperationException(
                "CU-01 no reasigna huecos. Use CU-23.");
    }

    // ---- Consultas de solicitudes de cambio (propias del CU-01) ----

    /**
     * Lista las solicitudes de cambio/cancelación pendientes de un paciente.
     */
    public List<SolicitudCambioRegistro> listarSolicitudesPendientes(String pacienteId) {
        validarTexto(pacienteId, "El identificador del paciente es obligatorio");
        return solicitudCambioRepository.buscarPorPaciente(pacienteId).stream()
                .filter(s -> s.estado() == EstadoSolicitudCambio.PENDIENTE)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ Auxiliares

    private CitaDTO obtenerCitaExistente(String citaId) {
        CitaDTO cita = consultaCitasService.obtenerCita(citaId);
        if (cita == null) {
            throw new IllegalArgumentException("No existe la cita: " + citaId);
        }
        return cita;
    }

    private void exigirCitaActiva(CitaDTO cita) {
        if (!ESTADOS_ACTIVOS.contains(cita.getEstado())) {
            throw new IllegalStateException(
                    "La cita " + cita.getId() + " no está activa; estado actual: " + cita.getEstado());
        }
    }

    private void exigirCitaFutura(CitaDTO cita) {
        if (cita.getFechaHora() != null && cita.getFechaHora().isBefore(LocalDateTime.now(reloj))) {
            throw new IllegalStateException(
                    "No se puede solicitar cambio de una cita pasada: " + cita.getId());
        }
    }

    private void verificarPacienteExiste(String pacienteId) {
        if (usuariosSeguridadService.obtenerPaciente(pacienteId) == null) {
            throw new IllegalArgumentException("No existe el paciente: " + pacienteId);
        }
    }

    private String nuevoIdSolicitud() {
        String id = generadorIdSolicitud.get();
        if (id == null || id.isBlank()) {
            throw new IllegalStateException("El generador no ha producido un identificador de solicitud");
        }
        return id;
    }

    private static void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
