package es.uclm.esi.iso2.sescam.citas.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Implementación en memoria (DAO simulado) del repositorio de turnos.
 */
public class TurnoRepositoryInMemory implements ITurnoRepository {

    private final Map<String, TurnoAgendaDTO> almacenamiento = new ConcurrentHashMap<>();

    @Override
    public TurnoAgendaDTO guardar(TurnoAgendaDTO turno) {
        if (turno == null) {
            throw new IllegalArgumentException("El turno no puede ser nulo");
        }
        almacenamiento.put(turno.idTurno(), turno);
        return turno;
    }

    @Override
    public void guardarTodos(List<TurnoAgendaDTO> turnos) {
        if (turnos == null) {
            throw new IllegalArgumentException("La lista de turnos no puede ser nula");
        }
        turnos.forEach(this::guardar);
    }

    @Override
    public Optional<TurnoAgendaDTO> buscarPorId(String idTurno) {
        return idTurno == null ? Optional.empty() : Optional.ofNullable(almacenamiento.get(idTurno));
    }

    @Override
    public List<TurnoAgendaDTO> buscarPorAgenda(String idAgenda) {
        return filtrar(t -> t.idAgenda().equals(idAgenda));
    }

    @Override
    public List<TurnoAgendaDTO> buscarPorRecursoYFecha(TipoRecurso tipoRecurso, String idRecurso, LocalDate fecha) {
        return filtrar(t -> t.tipoRecurso() == tipoRecurso
                && t.idRecurso().equals(idRecurso)
                && t.fecha().equals(fecha));
    }

    @Override
    public void eliminarPorAgenda(String idAgenda) {
        almacenamiento.values().removeIf(t -> t.idAgenda().equals(idAgenda));
    }

    private List<TurnoAgendaDTO> filtrar(Predicate<TurnoAgendaDTO> criterio) {
        return almacenamiento.values().stream().filter(criterio).toList();
    }
}
