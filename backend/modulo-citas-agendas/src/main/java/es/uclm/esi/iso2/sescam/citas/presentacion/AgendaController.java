package es.uclm.esi.iso2.sescam.citas.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.AgendaMedicaDTO;
import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;
import es.uclm.esi.iso2.sescam.common.interfaces.DisponibilidadAgendaService;
import es.uclm.esi.iso2.sescam.common.interfaces.GestionAgendaService;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Controlador de presentación del CU-14 para el Personal Administrativo de Admisión.
 * Conecta las vistas de administración de agendas con los contratos de dominio.
 */
public class AgendaController {

    private final GestionAgendaService gestionAgendaService;
    private final DisponibilidadAgendaService disponibilidadAgendaService;

    public AgendaController(GestionAgendaService gestionAgendaService,
                            DisponibilidadAgendaService disponibilidadAgendaService) {
        this.gestionAgendaService = Objects.requireNonNull(gestionAgendaService,
                "El servicio de gestión de agendas no puede ser nulo");
        this.disponibilidadAgendaService = Objects.requireNonNull(disponibilidadAgendaService,
                "El servicio de disponibilidad de agendas no puede ser nulo");
    }

    public AgendaMedicaDTO altaFranja(AgendaMedicaDTO franja, TipoRecurso tipoRecurso) {
        return gestionAgendaService.crearFranja(franja, tipoRecurso);
    }

    public AgendaMedicaDTO modificarFranja(AgendaMedicaDTO franja) {
        return gestionAgendaService.modificarFranja(franja);
    }

    public boolean eliminarFranja(String idAgenda) {
        return gestionAgendaService.eliminarAgenda(idAgenda);
    }

    public List<AgendaMedicaDTO> listarFranjas(TipoRecurso tipoRecurso, String idRecurso, LocalDate fecha) {
        return gestionAgendaService.listarFranjas(tipoRecurso, idRecurso, fecha);
    }

    public List<TurnoAgendaDTO> consultarAgenda(TipoRecurso tipoRecurso, String idRecurso, LocalDate fecha) {
        return gestionAgendaService.consultarAgenda(tipoRecurso, idRecurso, fecha);
    }

    public List<TurnoAgendaDTO> consultarHuecosLibres(TipoRecurso tipoRecurso, String idRecurso, LocalDate fecha) {
        return disponibilidadAgendaService.consultarHuecosLibres(tipoRecurso, idRecurso, fecha);
    }

    public TurnoAgendaDTO bloquearTurno(String idTurno, String motivo) {
        return gestionAgendaService.bloquearTurno(idTurno, motivo);
    }

    public TurnoAgendaDTO desbloquearTurno(String idTurno, String motivo) {
        return gestionAgendaService.desbloquearTurno(idTurno, motivo);
    }

    public List<TurnoAgendaDTO> bloquearFranja(String idAgenda, String motivo) {
        return gestionAgendaService.bloquearFranja(idAgenda, motivo);
    }
}
