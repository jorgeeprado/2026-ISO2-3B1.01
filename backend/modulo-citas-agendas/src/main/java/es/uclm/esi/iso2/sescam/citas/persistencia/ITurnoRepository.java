package es.uclm.esi.iso2.sescam.citas.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia de los turnos generados a partir de las franjas (CU-14).
 */
public interface ITurnoRepository {

    /** Guarda o reemplaza el turno con el mismo identificador. */
    TurnoAgendaDTO guardar(TurnoAgendaDTO turno);

    void guardarTodos(List<TurnoAgendaDTO> turnos);

    Optional<TurnoAgendaDTO> buscarPorId(String idTurno);

    List<TurnoAgendaDTO> buscarPorAgenda(String idAgenda);

    List<TurnoAgendaDTO> buscarPorRecursoYFecha(TipoRecurso tipoRecurso, String idRecurso, LocalDate fecha);

    void eliminarPorAgenda(String idAgenda);
}
