package es.uclm.esi.iso2.sescam.common.interfaces;

import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato de consulta de solicitudes y disponibilidad de agendas (CU-08).
 */
public interface ConsultaSolicitudesAgendasService {

	/** Devuelve las solicitudes emitidas por un médico, con su estado actual. */
	List<SolicitudPruebaDTO> consultarSolicitudesPorMedico(String medicoId);

	/** Devuelve los turnos libres de un recurso en una fecha. */
	List<TurnoAgendaDTO> consultarAgendasDisponibles(TipoRecurso tipoRecurso, String idRecurso, LocalDate fecha);
}
