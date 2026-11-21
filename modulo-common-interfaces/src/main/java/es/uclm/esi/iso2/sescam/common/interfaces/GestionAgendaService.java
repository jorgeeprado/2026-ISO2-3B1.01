package es.uclm.esi.iso2.sescam.common.interfaces;

import es.uclm.esi.iso2.sescam.common.dto.AgendaMedicaDTO;
import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato de administración de agendas diarias de médicos y salas (CU-14).
 * Usado por el Personal Administrativo de Admisión. Una franja es un {@link AgendaMedicaDTO}
 * cuyo campo {@code idMedico} identifica al recurso (médico o sala).
 *
 * <p>Errores: {@link IllegalArgumentException} ante datos inválidos o identificadores inexistentes;
 * {@link IllegalStateException} ante conflictos de negocio (solapes, turnos ocupados).</p>
 */
public interface GestionAgendaService extends AgendaService {

    /**
     * Da de alta una franja para un recurso y fecha y genera sus turnos.
     * Rechaza franjas que se solapen con otra del mismo recurso y fecha.
     */
    AgendaMedicaDTO crearFranja(AgendaMedicaDTO franja, TipoRecurso tipoRecurso);

    /**
     * Modifica una franja existente (mismo {@code idAgenda}) y regenera sus turnos,
     * conservando los bloqueos de los turnos que empiezan a la misma hora.
     * Rechaza la modificación si la franja tiene turnos ocupados o si genera solapes.
     */
    AgendaMedicaDTO modificarFranja(AgendaMedicaDTO franja);

    /**
     * Lista las franjas de un recurso en una fecha, ordenadas por hora de inicio.
     */
    List<AgendaMedicaDTO> listarFranjas(TipoRecurso tipoRecurso, String idRecurso, LocalDate fecha);

    /**
     * Consulta la agenda completa (todos los turnos) de un recurso en una fecha, ordenada por hora.
     */
    List<TurnoAgendaDTO> consultarAgenda(TipoRecurso tipoRecurso, String idRecurso, LocalDate fecha);

    /**
     * Bloquea un turno libre indicando el motivo. No se pueden bloquear turnos ocupados.
     */
    TurnoAgendaDTO bloquearTurno(String idTurno, String motivo);

    /**
     * Desbloquea un turno bloqueado indicando el motivo.
     */
    TurnoAgendaDTO desbloquearTurno(String idTurno, String motivo);

    /**
     * Bloquea todos los turnos libres de una franja con el mismo motivo.
     *
     * @return los turnos que han quedado bloqueados por esta operación
     */
    List<TurnoAgendaDTO> bloquearFranja(String idAgenda, String motivo);
}
