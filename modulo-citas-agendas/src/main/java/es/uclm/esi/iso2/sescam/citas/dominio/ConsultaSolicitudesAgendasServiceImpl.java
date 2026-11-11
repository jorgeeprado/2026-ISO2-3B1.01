package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;
import es.uclm.esi.iso2.sescam.common.interfaces.ConsultaSolicitudesAgendasService;
import es.uclm.esi.iso2.sescam.common.interfaces.DisponibilidadAgendaService;
import es.uclm.esi.iso2.sescam.common.interfaces.IPrescripcionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Servicio de dominio del CU-08. Consulta el estado de las solicitudes en el módulo
 * propietario de las prescripciones y obtiene disponibilidad mediante el contrato de agendas.
 */
public class ConsultaSolicitudesAgendasServiceImpl implements ConsultaSolicitudesAgendasService {

    private final IPrescripcionService prescripcionService;
    private final DisponibilidadAgendaService disponibilidadAgendaService;

    public ConsultaSolicitudesAgendasServiceImpl(IPrescripcionService prescripcionService,
                                                 DisponibilidadAgendaService disponibilidadAgendaService) {
        this.prescripcionService = Objects.requireNonNull(prescripcionService,
                "El servicio de prescripción no puede ser nulo");
        this.disponibilidadAgendaService = Objects.requireNonNull(disponibilidadAgendaService,
                "El servicio de disponibilidad de agendas no puede ser nulo");
    }

    @Override
    public List<SolicitudPruebaDTO> consultarSolicitudesPorMedico(String medicoId) {
        validarTexto(medicoId, "El identificador del médico es obligatorio");
        List<SolicitudPruebaDTO> solicitudes = prescripcionService.listarSolicitudesPorMedico(medicoId);
        if (solicitudes == null) {
            throw new IllegalStateException("El servicio de prescripción devolvió una lista nula");
        }
        return solicitudes.stream()
                .map(ConsultaSolicitudesAgendasServiceImpl::copiarSolicitud)
                .toList();
    }

    @Override
    public List<TurnoAgendaDTO> consultarAgendasDisponibles(TipoRecurso tipoRecurso,
                                                             String idRecurso,
                                                             LocalDate fecha) {
        if (tipoRecurso == null) {
            throw new IllegalArgumentException("El tipo de recurso es obligatorio");
        }
        validarTexto(idRecurso, "El identificador del recurso es obligatorio");
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        return List.copyOf(disponibilidadAgendaService.consultarHuecosLibres(tipoRecurso, idRecurso, fecha));
    }

    private static SolicitudPruebaDTO copiarSolicitud(SolicitudPruebaDTO solicitud) {
        return new SolicitudPruebaDTO(solicitud.getId(), solicitud.getPacienteId(),
                solicitud.getMedicoPrescriptorId(), solicitud.getTipoPruebaId(), solicitud.getPrioridad(),
                solicitud.getIndicacionClinica(), solicitud.getJustificacionDiagnostica(),
                solicitud.getFechaSolicitud(), solicitud.getEstado(), solicitud.isEsSeguimientoPeriodico(),
                solicitud.getPeriodicidadMeses());
    }

    private static void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
