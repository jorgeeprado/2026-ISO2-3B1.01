package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.citas.persistencia.CitaRegistro;
import es.uclm.esi.iso2.sescam.citas.persistencia.CitaRepositoryInMemory;
import es.uclm.esi.iso2.sescam.citas.persistencia.ICitaRepository;
import es.uclm.esi.iso2.sescam.common.dto.AgendaMedicaDTO;
import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;
import es.uclm.esi.iso2.sescam.common.dto.EntradaListaEsperaDTO;
import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoCita;
import es.uclm.esi.iso2.sescam.common.enums.EstadoTurno;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;
import es.uclm.esi.iso2.sescam.common.interfaces.AgendaService;
import es.uclm.esi.iso2.sescam.common.interfaces.CandidatosListaEsperaService;
import es.uclm.esi.iso2.sescam.common.interfaces.CitacionAdministrativaService;
import es.uclm.esi.iso2.sescam.common.interfaces.ConsultaCitasService;
import es.uclm.esi.iso2.sescam.common.interfaces.DisponibilidadAgendaService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Controlador de dominio del CU-13: asignación, reprogramación y cancelación definitiva de citas
 * por el Personal Administrativo (RN-02). Orquesta los turnos de CU-14 y la lista de espera de CU-15
 * y, si falla un paso intermedio, deshace los anteriores en orden inverso (D-024).
 */
public class CitacionServiceImpl implements CitacionAdministrativaService, ConsultaCitasService {

    /** Máximo usado al consultar candidatos para que ninguna solicitud de la lista quede fuera. */
    static final int SIN_LIMITE = Integer.MAX_VALUE;

    /** Estados en los que una cita ocupa su turno y puede reprogramarse o cancelarse. */
    private static final Set<EstadoCita> ESTADOS_ACTIVOS = EnumSet.of(EstadoCita.PROGRAMADA, EstadoCita.CONFIRMADA);

    private final ICitaRepository citaRepository;
    private final DisponibilidadAgendaService disponibilidadAgendaService;
    private final AgendaService agendaService;
    private final CandidatosListaEsperaService candidatosListaEsperaService;
    private final Clock reloj;
    private final Supplier<String> generadorIdCita;

    public CitacionServiceImpl(DisponibilidadAgendaService disponibilidadAgendaService,
                               AgendaService agendaService,
                               CandidatosListaEsperaService candidatosListaEsperaService) {
        this(new CitaRepositoryInMemory(), disponibilidadAgendaService, agendaService, candidatosListaEsperaService,
                Clock.systemDefaultZone(), generadorSecuencial());
    }

    public CitacionServiceImpl(ICitaRepository citaRepository,
                               DisponibilidadAgendaService disponibilidadAgendaService,
                               AgendaService agendaService,
                               CandidatosListaEsperaService candidatosListaEsperaService,
                               Clock reloj,
                               Supplier<String> generadorIdCita) {
        this.citaRepository = Objects.requireNonNull(citaRepository, "El repositorio de citas no puede ser nulo");
        this.disponibilidadAgendaService = Objects.requireNonNull(disponibilidadAgendaService,
                "El servicio de disponibilidad de agendas no puede ser nulo");
        this.agendaService = Objects.requireNonNull(agendaService, "El servicio de agendas no puede ser nulo");
        this.candidatosListaEsperaService = Objects.requireNonNull(candidatosListaEsperaService,
                "El servicio de candidatos de la lista de espera no puede ser nulo");
        this.reloj = Objects.requireNonNull(reloj, "El reloj no puede ser nulo");
        this.generadorIdCita = Objects.requireNonNull(generadorIdCita, "El generador de identificadores no puede ser nulo");
    }

    /** Generador de identificadores legibles y consecutivos: CITA-000001, CITA-000002... */
    static Supplier<String> generadorSecuencial() {
        AtomicLong contador = new AtomicLong();
        return () -> String.format("CITA-%06d", contador.incrementAndGet());
    }

    // ------------------------------------------------------------------ CitacionAdministrativaService

    @Override
    public CitaDTO asignarCita(String idSolicitud, String idTurno, String motivo) {
        validarTexto(idSolicitud, "El identificador de la solicitud es obligatorio");
        validarTexto(idTurno, "El identificador del turno es obligatorio");

        // Sin límite, para que la solicitud se encuentre aunque esté al final de la lista.
        List<EntradaListaEsperaDTO> candidatos = candidatosListaEsperaService.proponerCandidatos(idTurno, SIN_LIMITE);
        int posicion = posicionDe(candidatos, idSolicitud);
        if (posicion < 0) {
            throw new IllegalStateException("La solicitud " + idSolicitud
                    + " no está en espera o no es compatible con el turno " + idTurno);
        }
        boolean saltaOrden = posicion > 0;
        if (saltaOrden && (motivo == null || motivo.isBlank())) {
            throw new IllegalArgumentException("Asignar a una solicitud que no es la primera candidata (RN-01) "
                    + "exige motivo");
        }
        TurnoAgendaDTO turno = disponibilidadAgendaService.obtenerTurno(idTurno);
        if (turno == null) {
            throw new IllegalStateException("El turno ha dejado de existir: " + idTurno);
        }
        exigirTurnoFuturo(turno);
        String idCita = nuevoIdCita();
        EntradaListaEsperaDTO entrada = candidatos.get(posicion);
        CitaDTO cita = new CitaDTO(idCita, idSolicitud, entrada.pacienteId(), entrada.tipoPruebaId(), null, 0,
                null, null, EstadoCita.PROGRAMADA, null);
        aplicarTurno(cita, turno);
        CitaRegistro registro = new CitaRegistro(cita, idTurno, saltaOrden ? motivo : null, null);

        disponibilidadAgendaService.ocuparTurno(idTurno, idCita);
        try {
            candidatosListaEsperaService.marcarCitada(idSolicitud, idCita);
        } catch (RuntimeException e) {
            compensar(e, () -> disponibilidadAgendaService.liberarTurno(idTurno));
            throw e;
        }
        try {
            citaRepository.guardar(registro);
        } catch (RuntimeException e) {
            compensar(e,
                    () -> candidatosListaEsperaService.reincorporar(idSolicitud, motivoCompensacion(idCita)),
                    () -> disponibilidadAgendaService.liberarTurno(idTurno));
            throw e;
        }
        return registro.cita();
    }

    @Override
    public CitaDTO reprogramarCita(String idCita, String idTurnoNuevo, String motivo) {
        validarTexto(idTurnoNuevo, "El identificador del turno nuevo es obligatorio");
        validarTexto(motivo, "El motivo de la reprogramación es obligatorio");
        CitaRegistro actual = buscarActiva(idCita);
        String idTurnoAntiguo = actual.idTurno();
        if (idTurnoNuevo.equals(idTurnoAntiguo)) {
            throw new IllegalArgumentException("El turno nuevo debe ser distinto del actual");
        }
        TurnoAgendaDTO turnoNuevo = disponibilidadAgendaService.obtenerTurno(idTurnoNuevo);
        if (turnoNuevo == null) {
            throw new IllegalArgumentException("No existe el turno: " + idTurnoNuevo);
        }
        if (turnoNuevo.estado() != EstadoTurno.LIBRE) {
            throw new IllegalStateException("El turno nuevo no está libre; estado actual: " + turnoNuevo.estado());
        }
        exigirTurnoFuturo(turnoNuevo);
        CitaDTO cita = actual.cita();
        exigirCompatible(turnoNuevo, cita.getTipoPruebaId());
        aplicarTurno(cita, turnoNuevo);
        cita.setEstado(EstadoCita.PROGRAMADA);
        CitaRegistro reprogramada = new CitaRegistro(cita, idTurnoNuevo, actual.motivoAsignacion(), motivo);

        disponibilidadAgendaService.ocuparTurno(idTurnoNuevo, idCita);
        try {
            disponibilidadAgendaService.liberarTurno(idTurnoAntiguo);
        } catch (RuntimeException e) {
            compensar(e, () -> disponibilidadAgendaService.liberarTurno(idTurnoNuevo));
            throw e;
        }
        try {
            citaRepository.guardar(reprogramada);
        } catch (RuntimeException e) {
            compensar(e,
                    () -> disponibilidadAgendaService.ocuparTurno(idTurnoAntiguo, idCita),
                    () -> disponibilidadAgendaService.liberarTurno(idTurnoNuevo));
            throw e;
        }
        return reprogramada.cita();
    }

    @Override
    public CitaDTO cancelarCita(String idCita, String motivo) {
        validarTexto(motivo, "El motivo de la cancelación es obligatorio");
        CitaRegistro actual = buscarActiva(idCita);
        String idTurno = actual.idTurno();
        String idSolicitud = actual.idSolicitud();
        CitaDTO cita = actual.cita();
        cita.setEstado(EstadoCita.CANCELADA);
        cita.setMotivoCancelacion(motivo);
        CitaRegistro cancelada = new CitaRegistro(cita, idTurno, actual.motivoAsignacion(),
                actual.motivoReprogramacion());

        disponibilidadAgendaService.liberarTurno(idTurno);
        try {
            candidatosListaEsperaService.reincorporar(idSolicitud, motivo);
        } catch (RuntimeException e) {
            compensar(e, () -> disponibilidadAgendaService.ocuparTurno(idTurno, idCita));
            throw e;
        }
        try {
            citaRepository.guardar(cancelada);
        } catch (RuntimeException e) {
            compensar(e,
                    () -> candidatosListaEsperaService.marcarCitada(idSolicitud, idCita),
                    () -> disponibilidadAgendaService.ocuparTurno(idTurno, idCita));
            throw e;
        }
        return cancelada.cita();
    }

    // ------------------------------------------------------------------ ConsultaCitasService

    @Override
    public CitaDTO obtenerCita(String idCita) {
        validarTexto(idCita, "El identificador de la cita es obligatorio");
        return citaRepository.buscarPorId(idCita).map(CitaRegistro::cita).orElse(null);
    }

    @Override
    public List<CitaDTO> listarCitasPorSolicitud(String idSolicitud) {
        validarTexto(idSolicitud, "El identificador de la solicitud es obligatorio");
        return citaRepository.buscarPorSolicitud(idSolicitud).stream()
                .sorted(Comparator.comparing(CitaRegistro::idCita))
                .map(CitaRegistro::cita)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // ------------------------------------------------------------------ Auxiliares

    private CitaRegistro buscarActiva(String idCita) {
        validarTexto(idCita, "El identificador de la cita es obligatorio");
        CitaRegistro registro = citaRepository.buscarPorId(idCita)
                .orElseThrow(() -> new IllegalArgumentException("No existe la cita: " + idCita));
        EstadoCita estado = registro.cita().getEstado();
        if (!ESTADOS_ACTIVOS.contains(estado)) {
            throw new IllegalStateException("La cita " + idCita + " no está activa; estado actual: " + estado);
        }
        return registro;
    }

    private String nuevoIdCita() {
        String idCita = generadorIdCita.get();
        if (idCita == null || idCita.isBlank()) {
            throw new IllegalStateException("El generador no ha producido un identificador de cita");
        }
        if (citaRepository.buscarPorId(idCita).isPresent()) {
            throw new IllegalStateException("El identificador de cita generado ya existe: " + idCita);
        }
        return idCita;
    }

    private void exigirTurnoFuturo(TurnoAgendaDTO turno) {
        LocalDateTime inicio = LocalDateTime.of(turno.fecha(), turno.horaInicio());
        if (inicio.isBefore(LocalDateTime.now(reloj))) {
            throw new IllegalStateException("No se puede citar en un turno pasado: " + turno.idTurno());
        }
    }

    /** El turno es compatible si su franja no tiene prueba asociada o es la del tipo de prueba de la cita. */
    private void exigirCompatible(TurnoAgendaDTO turno, String tipoPruebaId) {
        AgendaMedicaDTO franja = agendaService.obtenerAgendaPorId(turno.idAgenda());
        if (franja == null) {
            throw new IllegalStateException("No existe la franja del turno: " + turno.idAgenda());
        }
        String prueba = franja.idPruebaAsociada();
        if (prueba != null && !prueba.isBlank() && !prueba.equals(tipoPruebaId)) {
            throw new IllegalStateException("El turno " + turno.idTurno() + " es de la prueba " + prueba
                    + " y la cita es de " + tipoPruebaId);
        }
    }

    /** Copia en la cita la fecha, duración y recurso del turno; el campo del otro recurso queda nulo (D-023). */
    private static void aplicarTurno(CitaDTO cita, TurnoAgendaDTO turno) {
        cita.setFechaHora(LocalDateTime.of(turno.fecha(), turno.horaInicio()));
        cita.setDuracionMinutos((int) ChronoUnit.MINUTES.between(turno.horaInicio(), turno.horaFin()));
        boolean esSala = turno.tipoRecurso() == TipoRecurso.SALA;
        cita.setSalaId(esSala ? turno.idRecurso() : null);
        cita.setMedicoRadiologoId(esSala ? null : turno.idRecurso());
    }

    private static int posicionDe(List<EntradaListaEsperaDTO> candidatos, String idSolicitud) {
        for (int i = 0; i < candidatos.size(); i++) {
            if (candidatos.get(i).idSolicitud().equals(idSolicitud)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Ejecuta las acciones de compensación en el orden dado. Si alguna falla, su excepción se añade
     * como suprimida a la original y se continúa con las siguientes (D-024).
     */
    private static void compensar(RuntimeException original, Runnable... acciones) {
        for (Runnable accion : acciones) {
            try {
                accion.run();
            } catch (RuntimeException fallo) {
                original.addSuppressed(fallo);
            }
        }
    }

    private static String motivoCompensacion(String idCita) {
        return "Compensación: no se pudo registrar la cita " + idCita;
    }

    private static void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
