package es.uclm.esi.iso2.sescam.citas.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoTurno;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Adaptador de consulta de CU-08 sobre el almacenamiento de turnos existente. */
public class ConsultaAgendasRepositoryInMemory implements IConsultaAgendasRepository {

	private final ITurnoRepository turnoRepository;

	public ConsultaAgendasRepositoryInMemory() {
		this(new TurnoRepositoryInMemory());
	}

	public ConsultaAgendasRepositoryInMemory(ITurnoRepository turnoRepository) {
		this.turnoRepository = Objects.requireNonNull(turnoRepository,
				"El repositorio de turnos no puede ser nulo");
	}

	@Override
	public List<TurnoAgendaDTO> consultarAgendasDisponibles(TipoRecurso tipoRecurso,
															 String idRecurso,
															 LocalDate fecha) {
		if (tipoRecurso == null) {
			throw new IllegalArgumentException("El tipo de recurso es obligatorio");
		}
		if (idRecurso == null || idRecurso.isBlank()) {
			throw new IllegalArgumentException("El identificador del recurso es obligatorio");
		}
		if (fecha == null) {
			throw new IllegalArgumentException("La fecha es obligatoria");
		}
		return turnoRepository.buscarPorRecursoYFecha(tipoRecurso, idRecurso, fecha).stream()
				.filter(turno -> turno.estado() == EstadoTurno.LIBRE)
				.sorted(Comparator.comparing(TurnoAgendaDTO::horaInicio))
				.toList();
	}
}
