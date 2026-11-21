package es.uclm.esi.iso2.sescam.analitica.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.AgendaMedicaDTO;
import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;
import es.uclm.esi.iso2.sescam.common.dto.EstadisticaOperativaDTO;
import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoSolicitud;
import es.uclm.esi.iso2.sescam.common.enums.ModalidadPrueba;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Adaptador temporal en memoria; las operaciones devuelven copias para no
 * exponer las instancias mutables almacenadas.
 */
public class EstadisticasRepositorioEnMemoria implements IEstadisticasRepositorio {

    private final Map<String, SolicitudPruebaDTO> solicitudesPorId = new LinkedHashMap<>();
    private final Map<String, CitaDTO> citasPorId = new LinkedHashMap<>();
    private final Map<String, AgendaMedicaDTO> agendasPorId = new LinkedHashMap<>();
    private final Map<String, ModalidadPrueba> modalidadPorTipoPrueba = new LinkedHashMap<>();
    private final List<EstadisticaOperativaDTO> estadisticasOperativas = new ArrayList<>();

    @Override
    public synchronized void guardarSolicitud(SolicitudPruebaDTO solicitud) {
        Objects.requireNonNull(solicitud, "La solicitud no puede ser nula");
        validarIdentificador(solicitud.getId(), "El id de solicitud");
        solicitudesPorId.put(solicitud.getId(), copiar(solicitud));
    }

    @Override
    public synchronized List<SolicitudPruebaDTO> buscarSolicitudesPorMedico(String medicoId) {
        validarIdentificador(medicoId, "El id del facultativo");
        return solicitudesPorId.values().stream()
                .filter(solicitud -> medicoId.equals(solicitud.getMedicoPrescriptorId()))
                .map(EstadisticasRepositorioEnMemoria::copiar)
                .toList();
    }

    @Override
    public synchronized List<SolicitudPruebaDTO> buscarSolicitudesPorModalidad(ModalidadPrueba modalidad) {
        Objects.requireNonNull(modalidad, "La modalidad no puede ser nula");
        return solicitudesPorId.values().stream()
                .filter(solicitud -> modalidad.equals(modalidadPorTipoPrueba.get(solicitud.getTipoPruebaId())))
                .map(EstadisticasRepositorioEnMemoria::copiar)
                .toList();
    }

    @Override
    public synchronized List<SolicitudPruebaDTO> buscarSolicitudesPorEstado(EstadoSolicitud estado) {
        Objects.requireNonNull(estado, "El estado no puede ser nulo");
        return solicitudesPorId.values().stream()
                .filter(solicitud -> estado == solicitud.getEstado())
                .map(EstadisticasRepositorioEnMemoria::copiar)
                .toList();
    }

    @Override
    public synchronized List<SolicitudPruebaDTO> buscarTodasLasSolicitudes() {
        return solicitudesPorId.values().stream()
                .map(EstadisticasRepositorioEnMemoria::copiar)
                .toList();
    }

    @Override
    public synchronized SolicitudPruebaDTO buscarSolicitudPorId(String solicitudId) {
        validarIdentificador(solicitudId, "El id de solicitud");
        SolicitudPruebaDTO solicitud = solicitudesPorId.get(solicitudId);
        return solicitud == null ? null : copiar(solicitud);
    }

    @Override
    public synchronized List<SolicitudPruebaDTO> buscarSolicitudesEnPeriodo(
            LocalDate fechaInicio, LocalDate fechaFin) {
        validarIntervalo(fechaInicio, fechaFin);
        return solicitudesPorId.values().stream()
                .filter(solicitud -> solicitud.getFechaSolicitud() != null)
                .filter(solicitud -> estaEnPeriodo(solicitud.getFechaSolicitud().toLocalDate(), fechaInicio, fechaFin))
                .map(EstadisticasRepositorioEnMemoria::copiar)
                .toList();
    }

    @Override
    public synchronized void registrarModalidadTipoPrueba(String tipoPruebaId, ModalidadPrueba modalidad) {
        validarIdentificador(tipoPruebaId, "El id del tipo de prueba");
        Objects.requireNonNull(modalidad, "La modalidad no puede ser nula");
        modalidadPorTipoPrueba.put(tipoPruebaId, modalidad);
    }

    @Override
    public synchronized void guardarCita(CitaDTO cita) {
        Objects.requireNonNull(cita, "La cita no puede ser nula");
        validarIdentificador(cita.getId(), "El id de cita");
        Objects.requireNonNull(cita.getFechaHora(), "La fecha de la cita no puede ser nula");
        Objects.requireNonNull(cita.getEstado(), "El estado de la cita no puede ser nulo");
        if (cita.getDuracionMinutos() < 0) {
            throw new IllegalArgumentException("La duración de la cita no puede ser negativa");
        }
        citasPorId.put(cita.getId(), copiar(cita));
    }

    @Override
    public synchronized List<CitaDTO> buscarCitasEnPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        validarIntervalo(fechaInicio, fechaFin);
        return citasPorId.values().stream()
                .filter(cita -> estaEnPeriodo(cita.getFechaHora().toLocalDate(), fechaInicio, fechaFin))
                .map(EstadisticasRepositorioEnMemoria::copiar)
                .toList();
    }

    @Override
    public synchronized void guardarAgenda(AgendaMedicaDTO agenda) {
        Objects.requireNonNull(agenda, "La agenda no puede ser nula");
        if (agenda.intervaloMinutos() <= 0) {
            throw new IllegalArgumentException("El intervalo de la agenda debe ser positivo");
        }
        agendasPorId.put(agenda.idAgenda(), agenda);
    }

    @Override
    public synchronized List<AgendaMedicaDTO> buscarAgendasEnPeriodo(
            LocalDate fechaInicio, LocalDate fechaFin) {
        validarIntervalo(fechaInicio, fechaFin);
        return agendasPorId.values().stream()
                .filter(agenda -> estaEnPeriodo(agenda.fecha(), fechaInicio, fechaFin))
                .toList();
    }

    @Override
    public synchronized void guardarEstadisticaOperativa(EstadisticaOperativaDTO estadistica) {
        Objects.requireNonNull(estadistica, "La estadística operativa no puede ser nula");
        if (estadistica.getFechaInicio() == null || estadistica.getFechaFin() == null) {
            throw new IllegalArgumentException("La estadística debe incluir ambas fechas");
        }
        if (estadistica.getFechaInicio().isAfter(estadistica.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        estadisticasOperativas.removeIf(guardada ->
                guardada.getFechaInicio().equals(estadistica.getFechaInicio())
                        && guardada.getFechaFin().equals(estadistica.getFechaFin()));
        estadisticasOperativas.add(copiar(estadistica));
    }

    @Override
    public synchronized List<EstadisticaOperativaDTO> buscarEstadisticasOperativas(
            LocalDate fechaInicio, LocalDate fechaFin) {
        validarIntervalo(fechaInicio, fechaFin);
        return estadisticasOperativas.stream()
                .filter(estadistica -> !estadistica.getFechaInicio().isAfter(fechaFin)
                        && !estadistica.getFechaFin().isBefore(fechaInicio))
                .map(EstadisticasRepositorioEnMemoria::copiar)
                .toList();
    }

    @Override
    public synchronized List<EstadisticaOperativaDTO> buscarTodasLasEstadisticasOperativas() {
        return estadisticasOperativas.stream()
                .map(EstadisticasRepositorioEnMemoria::copiar)
                .toList();
    }

    private static void validarIntervalo(LocalDate fechaInicio, LocalDate fechaFin) {
        Objects.requireNonNull(fechaInicio, "La fecha de inicio no puede ser nula");
        Objects.requireNonNull(fechaFin, "La fecha de fin no puede ser nula");
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }

    private static void validarIdentificador(String valor, String nombre) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(nombre + " es obligatorio");
        }
    }

    private static boolean estaEnPeriodo(LocalDate fecha, LocalDate inicio, LocalDate fin) {
        return !fecha.isBefore(inicio) && !fecha.isAfter(fin);
    }

    private static CitaDTO copiar(CitaDTO origen) {
        return new CitaDTO(
                origen.getId(),
                origen.getSolicitudId(),
                origen.getPacienteId(),
                origen.getTipoPruebaId(),
                origen.getFechaHora(),
                origen.getDuracionMinutos(),
                origen.getSalaId(),
                origen.getMedicoRadiologoId(),
                origen.getEstado(),
                origen.getMotivoCancelacion());
    }

    private static SolicitudPruebaDTO copiar(SolicitudPruebaDTO origen) {
        return new SolicitudPruebaDTO(
                origen.getId(),
                origen.getPacienteId(),
                origen.getMedicoPrescriptorId(),
                origen.getTipoPruebaId(),
                origen.getPrioridad(),
                origen.getIndicacionClinica(),
                origen.getJustificacionDiagnostica(),
                origen.getFechaSolicitud(),
                origen.getEstado(),
                origen.isEsSeguimientoPeriodico(),
                origen.getPeriodicidadMeses());
    }

    private static EstadisticaOperativaDTO copiar(EstadisticaOperativaDTO origen) {
        EstadisticaOperativaDTO copia = new EstadisticaOperativaDTO(
                origen.getFechaInicio(),
                origen.getFechaFin(),
                origen.getTotalCitasProgramadas(),
                origen.getTotalCitasRealizadas(),
                origen.getTotalCitasCanceladas(),
                origen.getTiempoMedioEsperaDias(),
                origen.getPorcentajeOcupacionAgendas(),
                origen.getSolicitudesPendientesEnListaEspera());
        copia.setPorcentajeCancelaciones(origen.getPorcentajeCancelaciones());
        return copia;
    }
}
