package es.uclm.esi.iso2.sescam.citas.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;
import es.uclm.esi.iso2.sescam.common.interfaces.ConsultaSolicitudesAgendasService;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/** Controlador de presentación para las consultas del CU-08. */
public class ConsultaSolicitudesAgendasController {

    private final ConsultaSolicitudesAgendasService consultaService;

    public ConsultaSolicitudesAgendasController(ConsultaSolicitudesAgendasService consultaService) {
        this.consultaService = Objects.requireNonNull(consultaService,
                "El servicio de consulta CU-08 no puede ser nulo");
    }

    public List<SolicitudPruebaDTO> consultarSolicitudesPorMedico(String medicoId) {
        return consultaService.consultarSolicitudesPorMedico(medicoId);
    }

    public List<TurnoAgendaDTO> consultarAgendasDisponibles(TipoRecurso tipoRecurso,
                                                             String idRecurso,
                                                             LocalDate fecha) {
        return consultaService.consultarAgendasDisponibles(tipoRecurso, idRecurso, fecha);
    }
}
