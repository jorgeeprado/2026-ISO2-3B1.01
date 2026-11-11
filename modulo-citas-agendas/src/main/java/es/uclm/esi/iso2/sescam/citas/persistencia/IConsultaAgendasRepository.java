package es.uclm.esi.iso2.sescam.citas.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;

import java.time.LocalDate;
import java.util.List;

/** Contrato de consulta de los turnos libres para el CU-08. */
public interface IConsultaAgendasRepository {

	/** Devuelve los turnos libres de un recurso y fecha, ordenados por hora de inicio. */
	List<TurnoAgendaDTO> consultarAgendasDisponibles(TipoRecurso tipoRecurso, String idRecurso, LocalDate fecha);
}
