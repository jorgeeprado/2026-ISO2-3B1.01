package es.uclm.esi.iso2.sescam.analitica.dominio;

import es.uclm.esi.iso2.sescam.analitica.persistencia.IEstadisticasRepositorio;
import es.uclm.esi.iso2.sescam.common.dto.AgendaMedicaDTO;
import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;
import es.uclm.esi.iso2.sescam.common.dto.EstadisticaOperativaDTO;
import es.uclm.esi.iso2.sescam.common.dto.PruebaCatalogoDTO;
import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoCita;
import es.uclm.esi.iso2.sescam.common.enums.EstadoSolicitud;
import es.uclm.esi.iso2.sescam.common.enums.ModalidadPrueba;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;
import es.uclm.esi.iso2.sescam.common.interfaces.IAnaliticaEstadisticasService;
import es.uclm.esi.iso2.sescam.common.interfaces.ICatalogoConfiguracionService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementación de las métricas clínicas y operativas del módulo.
 */
public class AnaliticaEstadisticasServiceImpl implements IAnaliticaEstadisticasService {

    private static final double SEGUNDOS_POR_DIA = 86_400.0;
    private final IEstadisticasRepositorio repositorio;
    private final ICatalogoConfiguracionService catalogo;
    private final Clock clock;

    public AnaliticaEstadisticasServiceImpl(
            IEstadisticasRepositorio repositorio,
            ICatalogoConfiguracionService catalogo,
            Clock clock) {
        this.repositorio = Objects.requireNonNull(repositorio, "El repositorio no puede ser nulo");
        this.catalogo = Objects.requireNonNull(catalogo, "El servicio de catálogo no puede ser nulo");
        this.clock = Objects.requireNonNull(clock, "El reloj no puede ser nulo");
    }

    /**
     * La espera se mide desde la prescripción hasta el reloj actual solo para
     * solicitudes pendientes o citadas; el DTO no contiene una fecha de realización.
     */
    @Override
    public Map<String, Object> consultarEstadisticasPrescripcionMedico(String medicoId) {
        validarIdentificador(medicoId, "medicoId");
        List<SolicitudPruebaDTO> solicitudes = repositorio.buscarSolicitudesPorMedico(medicoId);
        validarColeccion(solicitudes, "El repositorio devolvió una colección de solicitudes nula");

        LocalDateTime ahora = LocalDateTime.now(clock);
        List<SolicitudPruebaDTO> enEspera = solicitudes.stream()
                .filter(AnaliticaEstadisticasServiceImpl::estaEnEspera)
                .toList();
        Map<String, Double> esperaPorPrioridad = calcularEsperaPorPrioridad(enEspera, ahora);
        Map<String, Integer> solicitudesPorPrioridad = contarPorPrioridad(solicitudes);
        Map<String, Integer> solicitudesPorModalidad = contarPorModalidad(medicoId, solicitudes);
        Map<String, Double> porcentajePorModalidad = calcularPorcentajes(solicitudesPorModalidad, solicitudes.size());

        long seguimientos = solicitudes.stream()
                .filter(SolicitudPruebaDTO::isEsSeguimientoPeriodico)
                .count();
        long seguimientosRealizados = solicitudes.stream()
                .filter(SolicitudPruebaDTO::isEsSeguimientoPeriodico)
                .filter(solicitud -> solicitud.getEstado() == EstadoSolicitud.COMPLETADA)
                .count();

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("medicoId", medicoId);
        resultado.put("totalSolicitudes", solicitudes.size());
        resultado.put("tiempoMedioEsperaDias", calcularMediaEspera(enEspera, ahora));
        resultado.put("tiempoMedioEsperaDiasPorPrioridad", esperaPorPrioridad);
        resultado.put("solicitudesPorPrioridad", solicitudesPorPrioridad);
        resultado.put("totalSeguimientosPrescritos", seguimientos);
        resultado.put("seguimientosRealizados", seguimientosRealizados);
        resultado.put("tasaAdherenciaSeguimientoPorcentaje",
                porcentaje(seguimientosRealizados, seguimientos));
        resultado.put("solicitudesPorModalidad", solicitudesPorModalidad);
        resultado.put("porcentajeSolicitudesPorModalidad", porcentajePorModalidad);
        return Map.copyOf(resultado);
    }

    @Override
    public EstadisticaOperativaDTO generarInformeOperativoAgendas(LocalDate fechaInicio, LocalDate fechaFin) {
        validarIntervalo(fechaInicio, fechaFin);
        List<CitaDTO> citas = repositorio.buscarCitasEnPeriodo(fechaInicio, fechaFin);
        List<AgendaMedicaDTO> agendas = repositorio.buscarAgendasEnPeriodo(fechaInicio, fechaFin);
        List<SolicitudPruebaDTO> solicitudes =
                repositorio.buscarSolicitudesEnPeriodo(fechaInicio, fechaFin);
        validarColeccion(citas, "El repositorio devolvió una colección de citas nula");
        validarColeccion(agendas, "El repositorio devolvió una colección de agendas nula");
        validarColeccion(solicitudes, "El repositorio devolvió una colección de solicitudes nula");
        citas.forEach(AnaliticaEstadisticasServiceImpl::validarCitaOperativa);
        solicitudes.forEach(solicitud ->
                Objects.requireNonNull(solicitud, "La colección contiene una solicitud nula"));

        long capacidadTotal = agendas.stream()
                .mapToLong(AnaliticaEstadisticasServiceImpl::calcularCapacidadAgenda)
                .sum();
        long citasProgramadas = citas.stream()
                .filter(AnaliticaEstadisticasServiceImpl::cuentaComoProgramada)
                .count();
        long citasRealizadas = citas.stream()
                .filter(cita -> cita.getEstado() == EstadoCita.REALIZADA)
                .count();
        long citasCanceladas = citas.stream()
                .filter(cita -> cita.getEstado() == EstadoCita.CANCELADA)
                .count();
        long citasOcupadas = citas.stream()
                .filter(AnaliticaEstadisticasServiceImpl::ocupaHueco)
                .count();

        List<Long> demorasSegundos = citas.stream()
                .filter(AnaliticaEstadisticasServiceImpl::ocupaHueco)
                .map(this::calcularDemoraSegundos)
                .toList();
        double demoraMediaDias = demorasSegundos.isEmpty() ? 0.0
                : demorasSegundos.stream().mapToLong(Long::longValue).average().orElse(0.0) / SEGUNDOS_POR_DIA;
        int pendientes = (int) solicitudes.stream()
                .filter(solicitud -> solicitud.getEstado() == EstadoSolicitud.PENDIENTE_CITACION)
                .count();

        EstadisticaOperativaDTO informe = new EstadisticaOperativaDTO(
                fechaInicio,
                fechaFin,
                Math.toIntExact(citasProgramadas),
                Math.toIntExact(citasRealizadas),
                Math.toIntExact(citasCanceladas),
                demoraMediaDias,
                porcentaje(citasOcupadas, capacidadTotal),
                pendientes);
        informe.setPorcentajeCancelaciones(porcentaje(citasCanceladas, citasProgramadas));
        repositorio.guardarEstadisticaOperativa(informe);
        return informe;
    }

    @Override
    public Map<String, Object> generarCuadroMandoGlobal() {
        List<EstadisticaOperativaDTO> estadisticas = repositorio.buscarTodasLasEstadisticasOperativas();
        List<SolicitudPruebaDTO> solicitudes = repositorio.buscarTodasLasSolicitudes();
        validarColeccion(estadisticas, "El repositorio devolvió una colección de estadísticas nula");
        validarColeccion(solicitudes, "El repositorio devolvió una colección de solicitudes nula");

        EstadisticaOperativaDTO agregado = agregarEstadisticas(estadisticas, null, null);
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("totalSolicitudes", solicitudes.size());
        resultado.put("totalCitasProgramadas", agregado.getTotalCitasProgramadas());
        resultado.put("totalCitasRealizadas", agregado.getTotalCitasRealizadas());
        resultado.put("totalCitasCanceladas", agregado.getTotalCitasCanceladas());
        resultado.put("porcentajeCancelaciones", agregado.getPorcentajeCancelaciones());
        resultado.put("tiempoMedioEsperaDias", agregado.getTiempoMedioEsperaDias());
        resultado.put("porcentajeOcupacionAgendas", agregado.getPorcentajeOcupacionAgendas());
        resultado.put("solicitudesPendientesEnListaEspera", agregado.getSolicitudesPendientesEnListaEspera());
        return Map.copyOf(resultado);
    }

    private Map<String, Integer> contarPorModalidad(
            String medicoId, List<SolicitudPruebaDTO> solicitudes) {
        Map<String, Integer> conteos = new LinkedHashMap<>();
        Map<String, ModalidadPrueba> modalidadesPorTipoPrueba = new HashMap<>();
        for (ModalidadPrueba modalidad : ModalidadPrueba.values()) {
            conteos.put(modalidad.name(), 0);
        }
        for (SolicitudPruebaDTO solicitud : solicitudes) {
            String tipoPruebaId = solicitud.getTipoPruebaId();
            validarIdentificador(tipoPruebaId, "tipoPruebaId de una solicitud de " + medicoId);
            ModalidadPrueba modalidad = modalidadesPorTipoPrueba.computeIfAbsent(tipoPruebaId, id -> {
                PruebaCatalogoDTO tipoPrueba = catalogo.obtenerTipoPrueba(id);
                if (tipoPrueba == null || tipoPrueba.getModalidad() == null) {
                    throw new IllegalStateException("No se pudo resolver la modalidad del tipo de prueba " + id);
                }
                repositorio.registrarModalidadTipoPrueba(id, tipoPrueba.getModalidad());
                return tipoPrueba.getModalidad();
            });
            conteos.compute(modalidad.name(), (clave, total) -> total + 1);
        }
        return Map.copyOf(conteos);
    }

    private static Map<String, Double> calcularPorcentajes(Map<String, Integer> conteos, int total) {
        Map<String, Double> porcentajes = new LinkedHashMap<>();
        conteos.forEach((modalidad, cantidad) ->
                porcentajes.put(modalidad, porcentaje(cantidad, total)));
        return Map.copyOf(porcentajes);
    }

    private static Map<String, Integer> contarPorPrioridad(List<SolicitudPruebaDTO> solicitudes) {
        Map<String, Integer> conteos = new LinkedHashMap<>();
        for (Prioridad prioridad : Prioridad.values()) {
            conteos.put(prioridad.name(), 0);
        }
        conteos.put("SIN_PRIORIDAD", 0);
        for (SolicitudPruebaDTO solicitud : solicitudes) {
            String clave = solicitud.getPrioridad() == null
                    ? "SIN_PRIORIDAD" : solicitud.getPrioridad().name();
            conteos.compute(clave, (ignorada, total) -> total + 1);
        }
        return Map.copyOf(conteos);
    }

    private static Map<String, Double> calcularEsperaPorPrioridad(
            List<SolicitudPruebaDTO> solicitudes, LocalDateTime ahora) {
        Map<String, Double> resultado = new LinkedHashMap<>();
        for (Prioridad prioridad : Prioridad.values()) {
            List<SolicitudPruebaDTO> delGrupo = solicitudes.stream()
                    .filter(solicitud -> prioridad == solicitud.getPrioridad())
                    .toList();
            resultado.put(prioridad.name(), calcularMediaEspera(delGrupo, ahora));
        }
        return Map.copyOf(resultado);
    }

    private static double calcularMediaEspera(List<SolicitudPruebaDTO> solicitudes, LocalDateTime ahora) {
        if (solicitudes.isEmpty()) {
            return 0.0;
        }
        double segundosAcumulados = solicitudes.stream()
                .mapToLong(solicitud -> {
                    if (solicitud.getFechaSolicitud() == null) {
                        throw new IllegalStateException("Una solicitud en espera no tiene fecha de solicitud");
                    }
                    long segundos = ChronoUnit.SECONDS.between(solicitud.getFechaSolicitud(), ahora);
                    if (segundos < 0) {
                        throw new IllegalStateException("Una solicitud en espera tiene fecha futura");
                    }
                    return segundos;
                })
                .sum();
        return segundosAcumulados / solicitudes.size() / SEGUNDOS_POR_DIA;
    }

    private static boolean estaEnEspera(SolicitudPruebaDTO solicitud) {
        return solicitud.getEstado() == EstadoSolicitud.PENDIENTE_CITACION
                || solicitud.getEstado() == EstadoSolicitud.CITADA;
    }

    private static double porcentaje(long numerador, long denominador) {
        return denominador == 0 ? 0.0 : (double) numerador * 100.0 / denominador;
    }

    private static long calcularCapacidadAgenda(AgendaMedicaDTO agenda) {
        Objects.requireNonNull(agenda, "La colección contiene una agenda nula");
        if (agenda.intervaloMinutos() <= 0) {
            throw new IllegalArgumentException("El intervalo de una agenda debe ser positivo");
        }
        long duracionMinutos = ChronoUnit.MINUTES.between(agenda.horaInicio(), agenda.horaFin());
        return duracionMinutos / agenda.intervaloMinutos();
    }

    private static void validarCitaOperativa(CitaDTO cita) {
        Objects.requireNonNull(cita, "La colección contiene una cita nula");
        validarIdentificador(cita.getId(), "El id de cita");
        Objects.requireNonNull(cita.getFechaHora(), "Una cita no tiene fecha y hora");
        Objects.requireNonNull(cita.getEstado(), "Una cita no tiene estado");
    }

    private static boolean cuentaComoProgramada(CitaDTO cita) {
        return cita.getEstado() == EstadoCita.PROGRAMADA
                || cita.getEstado() == EstadoCita.CONFIRMADA
                || cita.getEstado() == EstadoCita.REALIZADA
                || cita.getEstado() == EstadoCita.CANCELADA;
    }

    private static boolean ocupaHueco(CitaDTO cita) {
        return cita.getEstado() == EstadoCita.PROGRAMADA
                || cita.getEstado() == EstadoCita.CONFIRMADA
                || cita.getEstado() == EstadoCita.REALIZADA;
    }

    private long calcularDemoraSegundos(CitaDTO cita) {
        validarIdentificador(cita.getSolicitudId(), "solicitudId de una cita programada");
        SolicitudPruebaDTO solicitud = repositorio.buscarSolicitudPorId(cita.getSolicitudId());
        if (solicitud == null || solicitud.getFechaSolicitud() == null) {
            throw new IllegalStateException(
                    "No se puede calcular la demora de la cita " + cita.getId()
                            + ": falta la solicitud asociada o su fecha");
        }
        long demora = ChronoUnit.SECONDS.between(solicitud.getFechaSolicitud(), cita.getFechaHora());
        if (demora < 0) {
            throw new IllegalStateException("La fecha de citación precede a la fecha de solicitud");
        }
        return demora;
    }

    private static EstadisticaOperativaDTO agregarEstadisticas(
            List<EstadisticaOperativaDTO> estadisticas, LocalDate fechaInicio, LocalDate fechaFin) {
        int programadas = 0;
        int realizadas = 0;
        int canceladas = 0;
        int pendientes = 0;
        double esperaPonderada = 0.0;
        double ocupacionPonderada = 0.0;
        long pesoTotal = 0;
        for (EstadisticaOperativaDTO estadistica : estadisticas) {
            Objects.requireNonNull(estadistica, "La colección contiene una estadística nula");
            programadas += estadistica.getTotalCitasProgramadas();
            realizadas += estadistica.getTotalCitasRealizadas();
            canceladas += estadistica.getTotalCitasCanceladas();
            pendientes += estadistica.getSolicitudesPendientesEnListaEspera();
            int peso = Math.max(estadistica.getTotalCitasProgramadas(), 0);
            esperaPonderada += estadistica.getTiempoMedioEsperaDias() * peso;
            ocupacionPonderada += estadistica.getPorcentajeOcupacionAgendas() * peso;
            pesoTotal += peso;
        }
        double espera = pesoTotal == 0 ? 0.0 : esperaPonderada / pesoTotal;
        double ocupacion = pesoTotal == 0 ? 0.0 : ocupacionPonderada / pesoTotal;
        EstadisticaOperativaDTO agregado = new EstadisticaOperativaDTO(
                fechaInicio, fechaFin, programadas, realizadas, canceladas, espera, ocupacion, pendientes);
        agregado.setPorcentajeCancelaciones(porcentaje(canceladas, programadas));
        return agregado;
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

    private static void validarColeccion(List<?> valores, String mensaje) {
        Objects.requireNonNull(valores, mensaje);
    }
}
