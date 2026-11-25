package es.uclm.esi.iso2.sescam.prescripcion.dominio;

import es.uclm.esi.iso2.sescam.common.dto.PacienteDTO;
import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoSolicitud;
import es.uclm.esi.iso2.sescam.common.interfaces.IPrescripcionService;
import es.uclm.esi.iso2.sescam.common.interfaces.IUsuariosSeguridadService;
import es.uclm.esi.iso2.sescam.prescripcion.persistencia.IPrescripcionRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para prescripciones puntuales y seguimientos periódicos (CU-05, CU-06 y CU-07).
 */
public class PrescripcionService implements IPrescripcionService {

    private final IPrescripcionRepository repositorio;
    private final IUsuariosSeguridadService usuariosService;

    public PrescripcionService(IPrescripcionRepository repositorio, IUsuariosSeguridadService usuariosService) {
        this.repositorio = Objects.requireNonNull(repositorio, "El repositorio no puede ser nulo");
        this.usuariosService = Objects.requireNonNull(usuariosService, "El servicio de usuarios no puede ser nulo");
    }

    @Override
    public SolicitudPruebaDTO prescribirPrueba(SolicitudPruebaDTO solicitud) {
        validarSolicitud(solicitud);
        validarPaciente(solicitud.getPacienteId());
        return guardarNuevaPrescripcion(solicitud, false, null);
    }

    @Override
    public SolicitudPruebaDTO prescribirSeguimientoPeriodico(SolicitudPruebaDTO solicitud, int mesesPeriodicidad) {
        if (mesesPeriodicidad <= 0) {
            throw new IllegalArgumentException("La periodicidad debe ser un numero de meses positivo");
        }
        validarSolicitud(solicitud);
        validarPaciente(solicitud.getPacienteId());
        return guardarNuevaPrescripcion(solicitud, true, mesesPeriodicidad);
    }

    @Override
    public SolicitudPruebaDTO consultarSolicitud(String solicitudId) {
        validarTextoObligatorio(solicitudId, "El identificador de la solicitud es obligatorio");
        return repositorio.buscarPorId(solicitudId)
                .map(Prescripcion::aDTO)
                .orElseThrow(() -> new PrescripcionNoEncontradaException(solicitudId));
    }

    @Override
    public List<SolicitudPruebaDTO> listarSolicitudesPorPaciente(String pacienteId) {
        validarTextoObligatorio(pacienteId, "El identificador del paciente es obligatorio");
        return repositorio.listarPorPaciente(pacienteId).stream()
                .filter(this::estaActiva)
                .sorted(Comparator.comparing(Prescripcion::getPrioridad, Comparator.nullsLast(Enum::compareTo))
                        .thenComparing(Prescripcion::getFechaSolicitud, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Prescripcion::getId))
                .map(Prescripcion::aDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SolicitudPruebaDTO> listarSolicitudesPorMedico(String medicoId) {
        validarTextoObligatorio(medicoId, "El identificador del medico es obligatorio");
        return repositorio.listarPorMedico(medicoId).stream()
                .sorted(Comparator.comparing(Prescripcion::getFechaSolicitud,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(Prescripcion::aDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SolicitudPruebaDTO autogenerarSiguienteCicloSeguimiento(String solicitudOriginalId) {
        validarTextoObligatorio(solicitudOriginalId, "El identificador de la solicitud original es obligatorio");
        Prescripcion original = repositorio.buscarPorId(solicitudOriginalId)
                .orElseThrow(() -> new PrescripcionNoEncontradaException(solicitudOriginalId));
        if (!original.isSeguimientoPeriodico()) {
            throw new SeguimientoNoValidoException("La solicitud original no pertenece a un seguimiento periodico");
        }
        if (original.getEstado() != EstadoSolicitud.COMPLETADA) {
            throw new SeguimientoNoValidoException("Solo se puede generar el siguiente ciclo tras completar la solicitud");
        }
        if (original.getPeriodicidadMeses() == null || original.getPeriodicidadMeses() <= 0) {
            throw new SeguimientoNoValidoException("La solicitud original no tiene una periodicidad valida");
        }
        SolicitudPruebaDTO siguiente = original.aDTO();
        siguiente.setId(null);
        siguiente.setFechaSolicitud(original.getFechaSolicitud() == null
                ? LocalDateTime.now().plusMonths(original.getPeriodicidadMeses())
                : original.getFechaSolicitud().plusMonths(original.getPeriodicidadMeses()));
        return prescribirSeguimientoPeriodico(siguiente, original.getPeriodicidadMeses());
    }

    private SolicitudPruebaDTO guardarNuevaPrescripcion(SolicitudPruebaDTO solicitud,
                                                        boolean seguimientoPeriodico,
                                                        Integer periodicidadMeses) {
        String id = estaInformado(solicitud.getId()) ? solicitud.getId() : "PRES-" + UUID.randomUUID();
        LocalDateTime fecha = solicitud.getFechaSolicitud() == null
                ? LocalDateTime.now()
                : solicitud.getFechaSolicitud();
        SolicitudPruebaDTO nueva = new SolicitudPruebaDTO(
                id,
                solicitud.getPacienteId(),
                solicitud.getMedicoPrescriptorId(),
                solicitud.getTipoPruebaId(),
                solicitud.getPrioridad(),
                solicitud.getIndicacionClinica(),
                solicitud.getJustificacionDiagnostica(),
                fecha,
                EstadoSolicitud.PENDIENTE_CITACION,
                seguimientoPeriodico,
                periodicidadMeses);
        return repositorio.guardar(Prescripcion.desdeDTO(nueva)).aDTO();
    }

    private void validarSolicitud(SolicitudPruebaDTO solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException("La solicitud de prueba no puede ser nula");
        }
        validarTextoObligatorio(solicitud.getPacienteId(), "El identificador del paciente es obligatorio");
        validarTextoObligatorio(solicitud.getMedicoPrescriptorId(), "El identificador del medico es obligatorio");
        validarTextoObligatorio(solicitud.getTipoPruebaId(), "El identificador del tipo de prueba es obligatorio");
        validarTextoObligatorio(solicitud.getIndicacionClinica(), "La indicacion clinica es obligatoria");
        validarTextoObligatorio(solicitud.getJustificacionDiagnostica(),
                "La justificacion diagnostica es obligatoria");
        if (solicitud.getPrioridad() == null) {
            throw new IllegalArgumentException("La prioridad de la solicitud es obligatoria");
        }
    }

    private void validarPaciente(String pacienteId) {
        PacienteDTO paciente = usuariosService.obtenerPaciente(pacienteId);
        if (paciente == null) {
            throw new PacienteNoEncontradoException(pacienteId);
        }
    }

    private boolean estaActiva(Prescripcion prescripcion) {
        EstadoSolicitud estado = prescripcion.getEstado();
        return estado == EstadoSolicitud.PENDIENTE_CITACION
                || estado == EstadoSolicitud.CITADA
                || estado == EstadoSolicitud.EN_CURSO;
    }

    private void validarTextoObligatorio(String valor, String mensaje) {
        if (!estaInformado(valor)) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private boolean estaInformado(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }
}