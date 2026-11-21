package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.citas.persistencia.FranjaRegistro;
import es.uclm.esi.iso2.sescam.citas.persistencia.FranjaRepositoryInMemory;
import es.uclm.esi.iso2.sescam.citas.persistencia.IFranjaRepository;
import es.uclm.esi.iso2.sescam.citas.persistencia.ITurnoRepository;
import es.uclm.esi.iso2.sescam.citas.persistencia.TurnoRepositoryInMemory;
import es.uclm.esi.iso2.sescam.common.dto.AgendaMedicaDTO;
import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoTurno;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;
import es.uclm.esi.iso2.sescam.common.interfaces.DisponibilidadAgendaService;
import es.uclm.esi.iso2.sescam.common.interfaces.GestionAgendaService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Controlador de dominio del CU-14: administración de agendas diarias de médicos y salas.
 * Gestiona franjas por recurso y fecha, genera sus turnos, aplica bloqueos y expone
 * la disponibilidad (ocupar/liberar) que consumirá la citación administrativa (CU-13).
 */
public class AgendaServiceImpl implements GestionAgendaService, DisponibilidadAgendaService {

    /** Tipo de recurso asumido por las operaciones heredadas de {@code AgendaService} (D-011). */
    static final TipoRecurso TIPO_RECURSO_POR_DEFECTO = TipoRecurso.MEDICO;

    private static final DateTimeFormatter FORMATO_ID_TURNO = DateTimeFormatter.ofPattern("HH:mm");
    private static final Comparator<AgendaMedicaDTO> POR_HORA_FRANJA =
            Comparator.comparing(AgendaMedicaDTO::horaInicio);
    private static final Comparator<TurnoAgendaDTO> POR_HORA_TURNO =
            Comparator.comparing(TurnoAgendaDTO::horaInicio);

    private final IFranjaRepository franjaRepository;
    private final ITurnoRepository turnoRepository;

    public AgendaServiceImpl() {
        this(new FranjaRepositoryInMemory(), new TurnoRepositoryInMemory());
    }

    public AgendaServiceImpl(IFranjaRepository franjaRepository, ITurnoRepository turnoRepository) {
        this.franjaRepository = Objects.requireNonNull(franjaRepository, "El repositorio de franjas no puede ser nulo");
        this.turnoRepository = Objects.requireNonNull(turnoRepository, "El repositorio de turnos no puede ser nulo");
    }

    // ------------------------------------------------------------------ AgendaService (contrato heredado)

    /**
     * Alta de franja con el tipo de recurso por defecto. Por compatibilidad con el contrato previo,
     * si ya existe una franja con el mismo identificador se trata como una modificación (D-014).
     */
    @Override
    public AgendaMedicaDTO crearAgenda(AgendaMedicaDTO agenda) {
        validarFranja(agenda);
        if (franjaRepository.buscarPorId(agenda.idAgenda()).isPresent()) {
            return modificarFranja(agenda);
        }
        return crearFranja(agenda, TIPO_RECURSO_POR_DEFECTO);
    }

    @Override
    public AgendaMedicaDTO obtenerAgendaPorId(String idAgenda) {
        validarTexto(idAgenda, "El identificador de la agenda es obligatorio");
        return franjaRepository.buscarPorId(idAgenda).map(FranjaRegistro::franja).orElse(null);
    }

    @Override
    public List<AgendaMedicaDTO> listarAgendasPorMedico(String idMedico) {
        validarTexto(idMedico, "El identificador del médico o recurso es obligatorio");
        return aFranjasOrdenadas(franjaRepository.buscarPorRecurso(TipoRecurso.MEDICO, idMedico));
    }

    @Override
    public List<AgendaMedicaDTO> listarAgendasPorFecha(LocalDate fecha) {
        validarFecha(fecha);
        return aFranjasOrdenadas(franjaRepository.buscarPorFecha(fecha));
    }

    /**
     * Elimina una franja y sus turnos.
     *
     * @throws IllegalStateException si la franja tiene turnos ocupados por citas (D-014)
     */
    @Override
    public boolean eliminarAgenda(String idAgenda) {
        validarTexto(idAgenda, "El identificador de la agenda es obligatorio");
        if (franjaRepository.buscarPorId(idAgenda).isEmpty()) {
            return false;
        }
        if (tieneTurnosOcupados(turnoRepository.buscarPorAgenda(idAgenda))) {
            throw new IllegalStateException("No se puede eliminar una franja con turnos ocupados por citas");
        }
        turnoRepository.eliminarPorAgenda(idAgenda);
        return franjaRepository.eliminar(idAgenda);
    }

    // ------------------------------------------------------------------ GestionAgendaService

    @Override
    public AgendaMedicaDTO crearFranja(AgendaMedicaDTO franja, TipoRecurso tipoRecurso) {
        validarFranja(franja);
        validarTipo(tipoRecurso);
        if (franjaRepository.buscarPorId(franja.idAgenda()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una franja con ese identificador");
        }
        comprobarSinSolapes(franja, tipoRecurso);

        franjaRepository.guardar(new FranjaRegistro(franja, tipoRecurso));
        turnoRepository.guardarTodos(generarTurnos(franja, tipoRecurso, Map.of()));
        return franja;
    }

    @Override
    public AgendaMedicaDTO modificarFranja(AgendaMedicaDTO franja) {
        validarFranja(franja);
        FranjaRegistro existente = franjaRepository.buscarPorId(franja.idAgenda())
                .orElseThrow(() -> new IllegalArgumentException("No existe una franja con ese identificador"));
        TipoRecurso tipoRecurso = existente.tipoRecurso();
        List<TurnoAgendaDTO> turnosActuales = turnoRepository.buscarPorAgenda(franja.idAgenda());
        if (tieneTurnosOcupados(turnosActuales)) {
            throw new IllegalStateException("No se puede modificar una franja con turnos ocupados por citas");
        }
        comprobarSinSolapes(franja, tipoRecurso);

        Map<LocalTime, TurnoAgendaDTO> bloqueados = turnosActuales.stream()
                .filter(t -> t.estado() == EstadoTurno.BLOQUEADO)
                .collect(Collectors.toMap(TurnoAgendaDTO::horaInicio, Function.identity()));

        franjaRepository.guardar(new FranjaRegistro(franja, tipoRecurso));
        turnoRepository.eliminarPorAgenda(franja.idAgenda());
        turnoRepository.guardarTodos(generarTurnos(franja, tipoRecurso, bloqueados));
        return franja;
    }

    @Override
    public List<AgendaMedicaDTO> listarFranjas(TipoRecurso tipoRecurso, String idRecurso, LocalDate fecha) {
        validarRecursoYFecha(tipoRecurso, idRecurso, fecha);
        return aFranjasOrdenadas(franjaRepository.buscarPorRecursoYFecha(tipoRecurso, idRecurso, fecha));
    }

    @Override
    public List<TurnoAgendaDTO> consultarAgenda(TipoRecurso tipoRecurso, String idRecurso, LocalDate fecha) {
        validarRecursoYFecha(tipoRecurso, idRecurso, fecha);
        return ordenar(turnoRepository.buscarPorRecursoYFecha(tipoRecurso, idRecurso, fecha));
    }

    @Override
    public TurnoAgendaDTO bloquearTurno(String idTurno, String motivo) {
        validarTexto(motivo, "El motivo del bloqueo es obligatorio");
        TurnoAgendaDTO turno = turnoExistente(idTurno);
        if (turno.estado() != EstadoTurno.LIBRE) {
            throw new IllegalStateException("Solo se pueden bloquear turnos libres (estado actual: "
                    + turno.estado() + ")");
        }
        return turnoRepository.guardar(conEstado(turno, EstadoTurno.BLOQUEADO, motivo, null));
    }

    @Override
    public TurnoAgendaDTO desbloquearTurno(String idTurno, String motivo) {
        validarTexto(motivo, "El motivo del desbloqueo es obligatorio");
        TurnoAgendaDTO turno = turnoExistente(idTurno);
        if (turno.estado() != EstadoTurno.BLOQUEADO) {
            throw new IllegalStateException("Solo se pueden desbloquear turnos bloqueados (estado actual: "
                    + turno.estado() + ")");
        }
        return turnoRepository.guardar(conEstado(turno, EstadoTurno.LIBRE, motivo, null));
    }

    @Override
    public List<TurnoAgendaDTO> bloquearFranja(String idAgenda, String motivo) {
        validarTexto(idAgenda, "El identificador de la agenda es obligatorio");
        validarTexto(motivo, "El motivo del bloqueo es obligatorio");
        if (franjaRepository.buscarPorId(idAgenda).isEmpty()) {
            throw new IllegalArgumentException("No existe una franja con ese identificador");
        }
        List<TurnoAgendaDTO> bloqueados = new ArrayList<>();
        for (TurnoAgendaDTO turno : ordenar(turnoRepository.buscarPorAgenda(idAgenda))) {
            if (turno.estado() == EstadoTurno.LIBRE) {
                bloqueados.add(turnoRepository.guardar(conEstado(turno, EstadoTurno.BLOQUEADO, motivo, null)));
            }
        }
        return bloqueados;
    }

    // ------------------------------------------------------------------ DisponibilidadAgendaService

    @Override
    public List<TurnoAgendaDTO> consultarHuecosLibres(TipoRecurso tipoRecurso, String idRecurso, LocalDate fecha) {
        return consultarAgenda(tipoRecurso, idRecurso, fecha).stream()
                .filter(t -> t.estado() == EstadoTurno.LIBRE)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public TurnoAgendaDTO obtenerTurno(String idTurno) {
        validarTexto(idTurno, "El identificador del turno es obligatorio");
        return turnoRepository.buscarPorId(idTurno).orElse(null);
    }

    @Override
    public TurnoAgendaDTO ocuparTurno(String idTurno, String idCita) {
        validarTexto(idCita, "El identificador de la cita es obligatorio");
        TurnoAgendaDTO turno = turnoExistente(idTurno);
        if (turno.estado() != EstadoTurno.LIBRE) {
            throw new IllegalStateException("El turno no está libre (estado actual: " + turno.estado() + ")");
        }
        return turnoRepository.guardar(conEstado(turno, EstadoTurno.OCUPADO, null, idCita));
    }

    @Override
    public TurnoAgendaDTO liberarTurno(String idTurno) {
        TurnoAgendaDTO turno = turnoExistente(idTurno);
        if (turno.estado() != EstadoTurno.OCUPADO) {
            throw new IllegalStateException("El turno no está ocupado (estado actual: " + turno.estado() + ")");
        }
        return turnoRepository.guardar(conEstado(turno, EstadoTurno.LIBRE, null, null));
    }

    // ------------------------------------------------------------------ Lógica interna

    /**
     * Genera floor((fin - inicio) / intervalo) turnos consecutivos (D-012). Si existía un turno bloqueado
     * que empieza a la misma hora, el nuevo turno conserva el bloqueo y su motivo (D-014).
     */
    static List<TurnoAgendaDTO> generarTurnos(AgendaMedicaDTO franja, TipoRecurso tipoRecurso,
                                              Map<LocalTime, TurnoAgendaDTO> bloqueadosPrevios) {
        long numeroTurnos = ChronoUnit.MINUTES.between(franja.horaInicio(), franja.horaFin())
                / franja.intervaloMinutos();
        List<TurnoAgendaDTO> turnos = new ArrayList<>();
        for (long i = 0; i < numeroTurnos; i++) {
            LocalTime inicio = franja.horaInicio().plusMinutes(i * franja.intervaloMinutos());
            LocalTime fin = inicio.plusMinutes(franja.intervaloMinutos());
            TurnoAgendaDTO previo = bloqueadosPrevios.get(inicio);
            EstadoTurno estado = previo == null ? EstadoTurno.LIBRE : EstadoTurno.BLOQUEADO;
            String motivo = previo == null ? null : previo.motivo();
            turnos.add(new TurnoAgendaDTO(idTurno(franja.idAgenda(), inicio), franja.idAgenda(), tipoRecurso,
                    franja.idMedico(), franja.fecha(), inicio, fin, estado, motivo, null));
        }
        return turnos;
    }

    static String idTurno(String idAgenda, LocalTime inicio) {
        return idAgenda + "#" + inicio.format(FORMATO_ID_TURNO);
    }

    /** Dos franjas se solapan si sus intervalos semiabiertos [inicio, fin) intersecan (D-013). */
    static boolean seSolapan(AgendaMedicaDTO a, AgendaMedicaDTO b) {
        return a.horaInicio().isBefore(b.horaFin()) && b.horaInicio().isBefore(a.horaFin());
    }

    private void comprobarSinSolapes(AgendaMedicaDTO franja, TipoRecurso tipoRecurso) {
        for (FranjaRegistro otra : franjaRepository.buscarPorRecursoYFecha(
                tipoRecurso, franja.idMedico(), franja.fecha())) {
            if (!otra.idAgenda().equals(franja.idAgenda()) && seSolapan(franja, otra.franja())) {
                throw new IllegalStateException("La franja se solapa con la franja " + otra.idAgenda()
                        + " del mismo recurso y fecha");
            }
        }
    }

    private static boolean tieneTurnosOcupados(List<TurnoAgendaDTO> turnos) {
        return turnos.stream().anyMatch(t -> t.estado() == EstadoTurno.OCUPADO);
    }

    private TurnoAgendaDTO turnoExistente(String idTurno) {
        validarTexto(idTurno, "El identificador del turno es obligatorio");
        return turnoRepository.buscarPorId(idTurno)
                .orElseThrow(() -> new IllegalArgumentException("No existe un turno con ese identificador"));
    }

    private static TurnoAgendaDTO conEstado(TurnoAgendaDTO t, EstadoTurno estado, String motivo, String idCita) {
        return new TurnoAgendaDTO(t.idTurno(), t.idAgenda(), t.tipoRecurso(), t.idRecurso(), t.fecha(),
                t.horaInicio(), t.horaFin(), estado, motivo, idCita);
    }

    private static List<AgendaMedicaDTO> aFranjasOrdenadas(List<FranjaRegistro> registros) {
        return registros.stream().map(FranjaRegistro::franja).sorted(POR_HORA_FRANJA)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<TurnoAgendaDTO> ordenar(List<TurnoAgendaDTO> turnos) {
        return turnos.stream().sorted(POR_HORA_TURNO).collect(Collectors.toCollection(ArrayList::new));
    }

    private static void validarFranja(AgendaMedicaDTO franja) {
        if (franja == null) {
            throw new IllegalArgumentException("La agenda no puede ser nula");
        }
        if (franja.intervaloMinutos() <= 0) {
            throw new IllegalArgumentException("El intervalo debe ser mayor que cero");
        }
        if (ChronoUnit.MINUTES.between(franja.horaInicio(), franja.horaFin()) < franja.intervaloMinutos()) {
            throw new IllegalArgumentException("La franja debe admitir al menos un turno completo");
        }
    }

    private static void validarRecursoYFecha(TipoRecurso tipoRecurso, String idRecurso, LocalDate fecha) {
        validarTipo(tipoRecurso);
        validarTexto(idRecurso, "El identificador del recurso es obligatorio");
        validarFecha(fecha);
    }

    private static void validarTipo(TipoRecurso tipoRecurso) {
        if (tipoRecurso == null) {
            throw new IllegalArgumentException("El tipo de recurso es obligatorio");
        }
    }

    private static void validarFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de la agenda no puede ser nula");
        }
    }

    private static void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
